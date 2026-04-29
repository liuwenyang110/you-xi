import { PlayerColor } from '../models/types';
import type { BoardState, Piece, Position } from '../models/types';
import { cloneBoard } from './Board';
import { getPossibleMoves, isFlyingGeneral, isKingInCheck, getKingAttackers } from './Rules';

export interface Move {
  piece: Piece;
  from: Position;
  to: Position;
  captured?: Piece;
}

export class GameManager {
  public board: BoardState;
  public currentPlayer: PlayerColor;
  public moveHistory: Move[];
  public isGameOver: boolean;
  public winner: PlayerColor | null;
  public victoryType: string | null;

  constructor(initialBoard: BoardState) {
    this.board = cloneBoard(initialBoard);
    this.currentPlayer = PlayerColor.RED; // 红方先手
    this.moveHistory = [];
    this.isGameOver = false;
    this.winner = null;
    this.victoryType = null;
  }

  // 尝试行棋，如果合法则更新状态
  public tryMove(piece: Piece, targetX: number, targetY: number): boolean {
    if (this.isGameOver || piece.color !== this.currentPlayer) return false;

    const moves = getPossibleMoves(this.board, piece);
    const validMove = moves.find(m => m.x === targetX && m.y === targetY);
    if (!validMove) return false;

    // 虚拟执行走步，检查是否会导致自己被将军或两帅面对面（禁手）
    const nextBoard = cloneBoard(this.board);
    const fakePiece = nextBoard[piece.position.y][piece.position.x]!;
    
    nextBoard[piece.position.y][piece.position.x] = null;
    nextBoard[targetY][targetX] = fakePiece;
    fakePiece.position = { x: targetX, y: targetY };

    if (isKingInCheck(nextBoard, this.currentPlayer) || isFlyingGeneral(nextBoard)) {
      return false; // 禁手：送将或将帅照面
    }

    // 正式执行——先记录旧位置，再更新
    const oldPos = { x: piece.position.x, y: piece.position.y };
    const realCaptured = this.board[targetY][targetX];
    
    this.board[piece.position.y][piece.position.x] = null;
    this.board[targetY][targetX] = piece;
    piece.position = { x: targetX, y: targetY };

    this.moveHistory.push({
      piece,
      from: oldPos, // 历史记录：正确地用旧位置
      to: { x: targetX, y: targetY },
      captured: realCaptured ? realCaptured : undefined
    });

    // 切换回合
    this.currentPlayer = this.currentPlayer === PlayerColor.RED ? PlayerColor.BLACK : PlayerColor.RED;

    this.checkGameOver();

    return true;
  }

  private detectVictoryType(loserColor: PlayerColor): string {
    const attackers = getKingAttackers(this.board, loserColor);
    
    // 困毙（无子可动但没被将军）
    if (attackers.length === 0) return '困毙';
    
    // 经典杀法识别
    if (attackers.length === 2 && attackers.every(p => p.type === 'CHARIOT')) return '『双车错』绝杀';
    if (attackers.some(p => p.type === 'CANNON') && attackers.some(p => p.type === 'HORSE')) return '『马后炮』绝杀';
    if (attackers.length >= 2 && attackers.some(p => p.type === 'CHARIOT')) return '『重炮叠车』绝杀';
    if (attackers.some(p => p.type === 'CANNON')) return '『铁门栓』绝杀';
    if (attackers.some(p => p.type === 'HORSE')) return '『卧槽马』绝杀';
    if (attackers.some(p => p.type === 'CHARIOT')) return '『单车绝杀』';
    if (attackers.some(p => p.type === 'SOLDIER')) return '『小卒擒王』绝杀';
    return '绝杀';
  }

  private checkGameOver() {
    // 判断当前方是否无路可走（被将死或困毙）
    let hasValidMove = false;
    
    for (let y = 0; y < 10 && !hasValidMove; y++) {
      for (let x = 0; x < 9 && !hasValidMove; x++) {
        const p = this.board[y][x];
        if (p && p.color === this.currentPlayer) {
          const moves = getPossibleMoves(this.board, p);
          for (const m of moves) {
            // 虚拟执行
            const nextBoard = cloneBoard(this.board);
            const fakeP = nextBoard[y][x]!;
            nextBoard[y][x] = null;
            nextBoard[m.y][m.x] = fakeP;
            fakeP.position = { x: m.x, y: m.y };
            
            if (!isKingInCheck(nextBoard, this.currentPlayer) && !isFlyingGeneral(nextBoard)) {
              hasValidMove = true;
              break;
            }
          }
        }
      }
    }

    if (!hasValidMove) {
      this.isGameOver = true;
      this.winner = this.currentPlayer === PlayerColor.RED ? PlayerColor.BLACK : PlayerColor.RED;
      this.victoryType = this.detectVictoryType(this.currentPlayer);
    }
  }

  // AI 专用：直接执行移动并返回被吃的棋子（不检查规则、不切换回合）
  public doMoveDirectly(fromX: number, fromY: number, toX: number, toY: number): Piece | null {
    const piece = this.board[fromY][fromX]!;
    const captured = this.board[toY][toX];
    this.board[fromY][fromX] = null;
    this.board[toY][toX] = piece;
    piece.position = { x: toX, y: toY };
    return captured;
  }
}
