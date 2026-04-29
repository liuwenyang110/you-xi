import { PieceType, PlayerColor } from '../models/types';
import type { BoardState, Piece, Position } from '../models/types';

export const isOutOfBounds = (x: number, y: number): boolean => {
  return x < 0 || x > 8 || y < 0 || y > 9;
};

export const isInCastle = (color: PlayerColor, x: number, y: number): boolean => {
  if (x < 3 || x > 5) return false;
  if (color === PlayerColor.BLACK) {
    return y >= 0 && y <= 2;
  } else {
    return y >= 7 && y <= 9;
  }
};

export const isSameColor = (board: BoardState, x: number, y: number, color: PlayerColor): boolean => {
  const target = board[y][x];
  return target !== null && target.color === color;
};

// 获取所有合法落子点
export const getPossibleMoves = (board: BoardState, piece: Piece): Position[] => {
  const { type, color, position } = piece;
  const { x, y } = position;
  const moves: Position[] = [];

  const addMoveIfValid = (targetX: number, targetY: number, condition: boolean = true) => {
    if (!isOutOfBounds(targetX, targetY) && condition && !isSameColor(board, targetX, targetY, color)) {
      moves.push({ x: targetX, y: targetY });
    }
  };

  switch (type) {
    case PieceType.GENERAL:
      addMoveIfValid(x, y - 1, isInCastle(color, x, y - 1));
      addMoveIfValid(x, y + 1, isInCastle(color, x, y + 1));
      addMoveIfValid(x - 1, y, isInCastle(color, x - 1, y));
      addMoveIfValid(x + 1, y, isInCastle(color, x + 1, y));
      break;

    case PieceType.ADVISOR:
      addMoveIfValid(x - 1, y - 1, isInCastle(color, x - 1, y - 1));
      addMoveIfValid(x + 1, y - 1, isInCastle(color, x + 1, y - 1));
      addMoveIfValid(x - 1, y + 1, isInCastle(color, x - 1, y + 1));
      addMoveIfValid(x + 1, y + 1, isInCastle(color, x + 1, y + 1));
      break;

    case PieceType.ELEPHANT:
      const isRed = color === PlayerColor.RED;
      const isValidElephantSide = (ty: number) => isRed ? ty >= 5 : ty <= 4;
      
      if (!isOutOfBounds(x - 2, y - 2) && isValidElephantSide(y - 2) && board[y - 1][x - 1] === null) {
        addMoveIfValid(x - 2, y - 2);
      }
      if (!isOutOfBounds(x + 2, y - 2) && isValidElephantSide(y - 2) && board[y - 1][x + 1] === null) {
        addMoveIfValid(x + 2, y - 2);
      }
      if (!isOutOfBounds(x - 2, y + 2) && isValidElephantSide(y + 2) && board[y + 1][x - 1] === null) {
        addMoveIfValid(x - 2, y + 2);
      }
      if (!isOutOfBounds(x + 2, y + 2) && isValidElephantSide(y + 2) && board[y + 1][x + 1] === null) {
        addMoveIfValid(x + 2, y + 2);
      }
      break;

    case PieceType.HORSE:
      const horseMoves = [
        { dx: -1, dy: -2, bx: 0, by: -1 },
        { dx: 1, dy: -2, bx: 0, by: -1 },
        { dx: -2, dy: -1, bx: -1, by: 0 },
        { dx: 2, dy: -1, bx: 1, by: 0 },
        { dx: -2, dy: 1, bx: -1, by: 0 },
        { dx: 2, dy: 1, bx: 1, by: 0 },
        { dx: -1, dy: 2, bx: 0, by: 1 },
        { dx: 1, dy: 2, bx: 0, by: 1 },
      ];
      horseMoves.forEach(({ dx, dy, bx, by }) => {
        if (!isOutOfBounds(x + dx, y + dy) && board[y + by][x + bx] === null) {
          addMoveIfValid(x + dx, y + dy);
        }
      });
      break;

    case PieceType.CHARIOT:
      const dirs = [[0, -1], [0, 1], [-1, 0], [1, 0]];
      dirs.forEach(([dx, dy]) => {
        let nx = x + dx;
        let ny = y + dy;
        while (!isOutOfBounds(nx, ny)) {
          addMoveIfValid(nx, ny);
          if (board[ny][nx] !== null) break; 
          nx += dx;
          ny += dy;
        }
      });
      break;

    case PieceType.CANNON:
      const cannonDirs = [[0, -1], [0, 1], [-1, 0], [1, 0]];
      cannonDirs.forEach(([dx, dy]) => {
        let nx = x + dx;
        let ny = y + dy;
        let jump = false;
        while (!isOutOfBounds(nx, ny)) {
          const targetPiece = board[ny][nx];
          if (!jump) {
            if (targetPiece === null) {
              addMoveIfValid(nx, ny);
            } else {
              jump = true; 
            }
          } else {
            if (targetPiece !== null) {
              addMoveIfValid(nx, ny);
              break; 
            }
          }
          nx += dx;
          ny += dy;
        }
      });
      break;

    case PieceType.SOLDIER:
      const dir = color === PlayerColor.RED ? -1 : 1;
      const isCrossed = color === PlayerColor.RED ? y <= 4 : y >= 5;
      
      addMoveIfValid(x, y + dir);
      if (isCrossed) {
        addMoveIfValid(x - 1, y);
        addMoveIfValid(x + 1, y);
      }
      break;
  }

  return moves;
};

export const isFlyingGeneral = (board: BoardState): boolean => {
  let redGeneral: Position | null = null;
  let blackGeneral: Position | null = null;

  for (let y = 0; y < 10; y++) {
    for (let x = 3; x <= 5; x++) {
      const p = board[y][x];
      if (p && p.type === PieceType.GENERAL) {
        if (p.color === PlayerColor.RED) redGeneral = p.position;
        if (p.color === PlayerColor.BLACK) blackGeneral = p.position;
      }
    }
  }

  if (redGeneral && blackGeneral) {
    if (redGeneral.x === blackGeneral.x) {
      let blocks = 0;
      const miny = Math.min(redGeneral.y, blackGeneral.y);
      const maxy = Math.max(redGeneral.y, blackGeneral.y);
      for (let y = miny + 1; y < maxy; y++) {
        if (board[y][redGeneral.x] !== null) blocks++;
      }
      if (blocks === 0) return true;
    }
  }
  return false;
};

// Returns attackers of the given king
export const getKingAttackers = (board: BoardState, kingColor: PlayerColor): Piece[] => {
  const attackers: Piece[] = [];
  let kingPos: Position | null = null;
  for (let y = 0; y < 10; y++) {
    for (let x = 3; x <= 5; x++) {
      const p = board[y][x];
      if (p && p.type === PieceType.GENERAL && p.color === kingColor) {
        kingPos = p.position;
        break;
      }
    }
    if (kingPos) break;
  }
  
  if (!kingPos) return attackers; 

  const enemyColor = kingColor === PlayerColor.RED ? PlayerColor.BLACK : PlayerColor.RED;
  for (let y = 0; y < 10; y++) {
    for (let x = 0; x < 9; x++) {
      const p = board[y][x];
      if (p && p.color === enemyColor) {
        const moves = getPossibleMoves(board, p);
        if (moves.some(m => m.x === kingPos!.x && m.y === kingPos!.y)) {
          attackers.push(p);
        }
      }
    }
  }
  return attackers;
};

export const isKingInCheck = (board: BoardState, kingColor: PlayerColor): boolean => {
  return getKingAttackers(board, kingColor).length > 0;
};
