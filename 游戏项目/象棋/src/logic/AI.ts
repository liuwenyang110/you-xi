import { PieceType, PlayerColor } from '../models/types';
import type { BoardState, Piece } from '../models/types';
import { getPossibleMoves, isFlyingGeneral, isKingInCheck } from './Rules';
import { cloneBoard } from './Board';

// ============================================================
//  棋子基础子力价值
// ============================================================
const PIECE_VALUES: Record<PieceType, number> = {
  [PieceType.GENERAL]: 100000,
  [PieceType.CHARIOT]: 1000,
  [PieceType.CANNON]: 500,
  [PieceType.HORSE]: 450,
  [PieceType.ELEPHANT]: 200,
  [PieceType.ADVISOR]: 200,
  [PieceType.SOLDIER]: 100,
};

// ============================================================
//  位置加权表 (黑方视角, 红方自动翻转)
// ============================================================
const CHARIOT_POS: number[][] = [
  [14, 14, 12, 18, 16, 18, 12, 14, 14],
  [16, 20, 18, 24, 26, 24, 18, 20, 16],
  [12, 12, 12, 18, 18, 18, 12, 12, 12],
  [12, 18, 16, 22, 22, 22, 16, 18, 12],
  [12, 14, 12, 18, 18, 18, 12, 14, 12],
  [12, 16, 14, 20, 20, 20, 14, 16, 12],
  [ 6, 10, 8, 14, 14, 14,  8, 10,  6],
  [ 4,  8, 6, 14, 12, 14,  6,  8,  4],
  [ 8,  4, 8, 16, 8, 16,  8,  4,  8],
  [-2,  10, 6, 14, 12, 14,  6, 10, -2],
];

const HORSE_POS: number[][] = [
  [ 4,  8, 16, 12, 4, 12, 16,  8,  4],
  [ 4, 10, 28, 16, 8, 16, 28, 10,  4],
  [12, 14, 16, 20, 18, 20, 16, 14, 12],
  [ 8, 24, 18, 24, 20, 24, 18, 24,  8],
  [ 6, 16, 14, 18, 16, 18, 14, 16,  6],
  [ 4, 12, 16, 14, 12, 14, 16, 12,  4],
  [ 2,  6,  8, 6, 10,  6,  8,  6,  2],
  [ 4,  2, 8,  8,  4,  8,  8,  2,  4],
  [ 0,  2, 4,  4, -2,  4,  4,  2,  0],
  [ 0, -4, 0,  0,  0,  0,  0, -4,  0],
];

const CANNON_POS: number[][] = [
  [ 6,  4,  0,-10, 6,-10,  0,  4,  6],
  [ 2,  2,  0, -4, -8, -4,  0,  2,  2],
  [ 2,  2,  0,-10,-8,-10,  0,  2,  2],
  [ 0,  0, -2,  4, 10,  4, -2,  0,  0],
  [ 0,  0,  0,  2,  8,  2,  0,  0,  0],
  [-2,  0,  4,  2,  6,  2,  4,  0, -2],
  [ 0,  0,  0,  2,  4,  2,  0,  0,  0],
  [ 4,  0,  8,  6,  10, 6,  8,  0,  4],
  [ 0,  2,  4,  6,  6,  6,  4,  2,  0],
  [ 0,  0,  2,  6,  6,  6,  2,  0,  0],
];

const SOLDIER_POS: number[][] = [
  [ 0,  3,  6,  9, 12,  9,  6,  3,  0],
  [18, 36, 56, 80, 120, 80, 56, 36, 18],
  [14, 26, 42, 60,  80, 60, 42, 26, 14],
  [10, 20, 30, 34,  40, 34, 30, 20, 10],
  [ 6, 12, 18, 18,  20, 18, 18, 12,  6],
  [ 2,  0,  8,  0,   8,  0,  8,  0,  2],
  [ 0,  0, -2,  0,   4,  0, -2,  0,  0],
  [ 0,  0,  0,  0,   0,  0,  0,  0,  0],
  [ 0,  0,  0,  0,   0,  0,  0,  0,  0],
  [ 0,  0,  0,  0,   0,  0,  0,  0,  0],
];

const PIECE_POSITION_TABLES: Partial<Record<PieceType, number[][]>> = {
  [PieceType.CHARIOT]: CHARIOT_POS,
  [PieceType.HORSE]: HORSE_POS,
  [PieceType.CANNON]: CANNON_POS,
  [PieceType.SOLDIER]: SOLDIER_POS,
};



// ============================================================
//  局面评估函数
// ============================================================
export const evaluateBoard = (board: BoardState): number => {
  let score = 0;
  let blackMaterial = 0;
  let redMaterial = 0;
  let blackGeneralPos: {x: number, y: number} | null = null;
  let redGeneralPos: {x: number, y: number} | null = null;

  for (let y = 0; y < 10; y++) {
    for (let x = 0; x < 9; x++) {
      const p = board[y][x];
      if (p) {
        let val = PIECE_VALUES[p.type];

        // 位置加权
        const posTable = PIECE_POSITION_TABLES[p.type];
        if (posTable) {
          if (p.color === PlayerColor.BLACK) {
            val += posTable[y][x];
          } else {
            val += posTable[9 - y][8 - x];
          }
        }

        if (p.color === PlayerColor.BLACK) {
          score += val;
          if (p.type !== PieceType.GENERAL) blackMaterial += PIECE_VALUES[p.type];
          if (p.type === PieceType.GENERAL) blackGeneralPos = { x, y };
        } else {
          score -= val;
          if (p.type !== PieceType.GENERAL) redMaterial += PIECE_VALUES[p.type];
          if (p.type === PieceType.GENERAL) redGeneralPos = { x, y };
        }
      }
    }
  }

  // *** 关键修复：当优势巨大时，奖励逼近敌方将帅 ***
  if (blackGeneralPos && redGeneralPos) {
    const materialAdvantage = blackMaterial - redMaterial;
    
    if (materialAdvantage > 500) {
      // 黑方大优：鼓励黑子靠近红帅
      const dist = Math.abs(blackGeneralPos.x - redGeneralPos.x) + Math.abs(blackGeneralPos.y - redGeneralPos.y);
      score += (20 - dist) * 10; // 越近分越高
      
      // 奖励将军状态
      if (isKingInCheck(board, PlayerColor.RED)) {
        score += 300;
      }
    } else if (materialAdvantage < -500) {
      // 红方大优
      const dist = Math.abs(blackGeneralPos.x - redGeneralPos.x) + Math.abs(blackGeneralPos.y - redGeneralPos.y);
      score -= (20 - dist) * 10;
      
      if (isKingInCheck(board, PlayerColor.BLACK)) {
        score -= 300;
      }
    }
  }

  return score;
};

// ============================================================
//  Alpha-Beta 剪枝 Minimax
// ============================================================
const minimax = (
  board: BoardState,
  depth: number,
  alpha: number,
  beta: number,
  isMaximizingPlayer: boolean
): number => {
  if (depth === 0) {
    return evaluateBoard(board);
  }

  const currentColor = isMaximizingPlayer ? PlayerColor.BLACK : PlayerColor.RED;
  
  // 收集所有候选步骤
  const candidates: {fx: number, fy: number, tx: number, ty: number, capPiece: Piece | null}[] = [];
  
  for (let y = 0; y < 10; y++) {
    for (let x = 0; x < 9; x++) {
      const p = board[y][x];
      if (p && p.color === currentColor) {
        const moves = getPossibleMoves(board, p);
        for (const m of moves) {
          candidates.push({ fx: x, fy: y, tx: m.x, ty: m.y, capPiece: board[m.y][m.x] });
        }
      }
    }
  }

  // 启发式排序：吃子 > 其他
  candidates.sort((a, b) => {
    const capA = a.capPiece ? PIECE_VALUES[a.capPiece.type] : 0;
    const capB = b.capPiece ? PIECE_VALUES[b.capPiece.type] : 0;
    return capB - capA;
  });

  let bestVal = isMaximizingPlayer ? -Infinity : Infinity;
  let hasValid = false;

  for (const move of candidates) {
    // Make move
    const piece = board[move.fy][move.fx]!;
    const captured = board[move.ty][move.tx];
    
    board[move.fy][move.fx] = null;
    board[move.ty][move.tx] = piece;
    piece.position.x = move.tx;
    piece.position.y = move.ty;
    
    // 禁手判断
    const isCheck = isKingInCheck(board, currentColor);
    const isFlyingGen = isFlyingGeneral(board);

    if (!isCheck && !isFlyingGen) {
      hasValid = true;
      const val = minimax(board, depth - 1, alpha, beta, !isMaximizingPlayer);

      if (isMaximizingPlayer) {
        bestVal = Math.max(bestVal, val);
        alpha = Math.max(alpha, bestVal);
      } else {
        bestVal = Math.min(bestVal, val);
        beta = Math.min(beta, bestVal);
      }
    }

    // Unmake move
    piece.position.x = move.fx;
    piece.position.y = move.fy;
    board[move.ty][move.tx] = captured;
    board[move.fy][move.fx] = piece;

    if (hasValid && beta <= alpha) break;
  }

  if (!hasValid) {
    if (isKingInCheck(board, currentColor)) {
      // 被将死！根据深度返回极端分数（越浅越好=更快将死）
      return isMaximizingPlayer ? (-99999 - depth) : (99999 + depth);
    } else {
      // 困毙
      return isMaximizingPlayer ? (-50000 - depth) : (50000 + depth);
    }
  }

  return bestVal;
};

// ============================================================
//  对外接口：获取 AI 最佳步骤
// ============================================================

// 简单的上一次走法记录，防止无限重复
let lastAIMove: string = '';
let repeatCount = 0;

export const getBestMove = (board: BoardState, depth: number = 3): {f: {x:number, y:number}, t: {x:number, y:number}} | null => {
  const currentBoard = cloneBoard(board);

  let bestMove: {f: {x:number, y:number}, t: {x:number, y:number}} | null = null;
  let bestVal = -Infinity;
  let alpha = -Infinity;
  const beta = Infinity;

  // 收集顶层步骤
  const candidates: {fx: number, fy: number, tx: number, ty: number, capPiece: Piece | null}[] = [];
  for (let y = 0; y < 10; y++) {
    for (let x = 0; x < 9; x++) {
      const p = currentBoard[y][x];
      if (p && p.color === PlayerColor.BLACK) {
        getPossibleMoves(currentBoard, p).forEach(m => candidates.push({fx: x, fy: y, tx: m.x, ty: m.y, capPiece: currentBoard[m.y][m.x]}));
      }
    }
  }

  // 启发式排序
  candidates.sort((a, b) => {
    const capA = a.capPiece ? PIECE_VALUES[a.capPiece.type] : 0;
    const capB = b.capPiece ? PIECE_VALUES[b.capPiece.type] : 0;
    return capB - capA;
  });

  const results: {move: {f: {x:number, y:number}, t: {x:number, y:number}}, val: number}[] = [];

  for (const move of candidates) {
    // Make move
    const piece = currentBoard[move.fy][move.fx]!;
    const captured = currentBoard[move.ty][move.tx];
    
    currentBoard[move.fy][move.fx] = null;
    currentBoard[move.ty][move.tx] = piece;
    piece.position.x = move.tx;
    piece.position.y = move.ty;
    
    const isCheck = isKingInCheck(currentBoard, PlayerColor.BLACK);
    const isFlyingGen = isFlyingGeneral(currentBoard);

    if (!isCheck && !isFlyingGen) {
      const val = minimax(currentBoard, depth - 1, alpha, beta, false);
      
      results.push({
        move: { f: {x: move.fx, y: move.fy}, t: {x: move.tx, y: move.ty} },
        val
      });

      if (val > bestVal) {
        bestVal = val;
        bestMove = { f: {x: move.fx, y: move.fy}, t: {x: move.tx, y: move.ty} };
      }
      alpha = Math.max(alpha, bestVal);
    }

    // Unmake Move
    piece.position.x = move.fx;
    piece.position.y = move.fy;
    currentBoard[move.ty][move.tx] = captured;
    currentBoard[move.fy][move.fx] = piece;
  }

  // *** 关键修复：防止重复走法 ***
  if (bestMove) {
    const moveKey = `${bestMove.f.x},${bestMove.f.y}-${bestMove.t.x},${bestMove.t.y}`;
    if (moveKey === lastAIMove) {
      repeatCount++;
      if (repeatCount >= 2) {
        // 如果连续重复同一步超过2次，换一步走
        const alternatives = results
          .filter(r => `${r.move.f.x},${r.move.f.y}-${r.move.t.x},${r.move.t.y}` !== moveKey)
          .sort((a, b) => b.val - a.val);
        if (alternatives.length > 0) {
          bestMove = alternatives[0].move;
          repeatCount = 0;
        }
      }
    } else {
      repeatCount = 0;
    }
    lastAIMove = `${bestMove.f.x},${bestMove.f.y}-${bestMove.t.x},${bestMove.t.y}`;
  }

  return bestMove;
};

// 重置AI状态（新游戏时调用）
export const resetAIState = () => {
  lastAIMove = '';
  repeatCount = 0;
};
