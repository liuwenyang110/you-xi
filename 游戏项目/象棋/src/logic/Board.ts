import { PieceType, PlayerColor } from '../models/types';
import type { BoardState } from '../models/types';

export const createInitialBoard = (): BoardState => {
  const board: BoardState = Array(10).fill(null).map(() => Array(9).fill(null));

  const addPiece = (type: PieceType, color: PlayerColor, x: number, y: number, idPrefix: string) => {
    board[y][x] = {
      id: `${color}-${idPrefix}-${x}-${y}`,
      type,
      color,
      position: { x, y }
    };
  };

  // Black pieces (Top)
  addPiece(PieceType.CHARIOT, PlayerColor.BLACK, 0, 0, 'C1');
  addPiece(PieceType.HORSE, PlayerColor.BLACK, 1, 0, 'H1');
  addPiece(PieceType.ELEPHANT, PlayerColor.BLACK, 2, 0, 'E1');
  addPiece(PieceType.ADVISOR, PlayerColor.BLACK, 3, 0, 'A1');
  addPiece(PieceType.GENERAL, PlayerColor.BLACK, 4, 0, 'G');
  addPiece(PieceType.ADVISOR, PlayerColor.BLACK, 5, 0, 'A2');
  addPiece(PieceType.ELEPHANT, PlayerColor.BLACK, 6, 0, 'E2');
  addPiece(PieceType.HORSE, PlayerColor.BLACK, 7, 0, 'H2');
  addPiece(PieceType.CHARIOT, PlayerColor.BLACK, 8, 0, 'C2');

  addPiece(PieceType.CANNON, PlayerColor.BLACK, 1, 2, 'CN1');
  addPiece(PieceType.CANNON, PlayerColor.BLACK, 7, 2, 'CN2');

  for (let i = 0; i < 5; i++) {
    addPiece(PieceType.SOLDIER, PlayerColor.BLACK, i * 2, 3, `S${i}`);
  }

  // Red pieces (Bottom)
  addPiece(PieceType.CHARIOT, PlayerColor.RED, 0, 9, 'C1');
  addPiece(PieceType.HORSE, PlayerColor.RED, 1, 9, 'H1');
  addPiece(PieceType.ELEPHANT, PlayerColor.RED, 2, 9, 'E1');
  addPiece(PieceType.ADVISOR, PlayerColor.RED, 3, 9, 'A1');
  addPiece(PieceType.GENERAL, PlayerColor.RED, 4, 9, 'G');
  addPiece(PieceType.ADVISOR, PlayerColor.RED, 5, 9, 'A2');
  addPiece(PieceType.ELEPHANT, PlayerColor.RED, 6, 9, 'E2');
  addPiece(PieceType.HORSE, PlayerColor.RED, 7, 9, 'H2');
  addPiece(PieceType.CHARIOT, PlayerColor.RED, 8, 9, 'C2');

  addPiece(PieceType.CANNON, PlayerColor.RED, 1, 7, 'CN1');
  addPiece(PieceType.CANNON, PlayerColor.RED, 7, 7, 'CN2');

  for (let i = 0; i < 5; i++) {
    addPiece(PieceType.SOLDIER, PlayerColor.RED, i * 2, 6, `S${i}`);
  }

  return board;
};

// Deep copy the board to avoid mutating states directly
export const cloneBoard = (board: BoardState): BoardState => {
  return board.map(row => row.map(cell => cell ? { ...cell, position: { ...cell.position } } : null));
};
