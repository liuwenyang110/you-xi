export const PlayerColor = {
  RED: 'RED',
  BLACK: 'BLACK',
} as const;
export type PlayerColor = typeof PlayerColor[keyof typeof PlayerColor];

export const PieceType = {
  GENERAL: 'GENERAL',
  ADVISOR: 'ADVISOR',
  ELEPHANT: 'ELEPHANT',
  HORSE: 'HORSE',
  CHARIOT: 'CHARIOT',
  CANNON: 'CANNON',
  SOLDIER: 'SOLDIER',
} as const;
export type PieceType = typeof PieceType[keyof typeof PieceType];

export interface Position {
  x: number;
  y: number;
}
export interface Piece {
  id: string;
  type: PieceType;
  color: PlayerColor;
  position: Position;
}
export type BoardState = (Piece | null)[][];

export const getPieceLabel = (piece: Piece): string => {
  const isRed = piece.color === PlayerColor.RED;
  switch (piece.type) {
    case PieceType.GENERAL: return isRed ? '帅' : '将';
    case PieceType.ADVISOR: return isRed ? '仕' : '士';
    case PieceType.ELEPHANT: return isRed ? '相' : '象';
    case PieceType.HORSE: return '马';
    case PieceType.CHARIOT: return '车';
    case PieceType.CANNON: return '炮';
    case PieceType.SOLDIER: return isRed ? '兵' : '卒';
    default: return '';
  }
};
