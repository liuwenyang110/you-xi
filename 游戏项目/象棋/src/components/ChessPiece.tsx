import React from 'react';
import { PlayerColor, getPieceLabel } from '../models/types';
import type { Piece } from '../models/types';
import './ChessPiece.css';

interface ChessPieceProps {
  piece: Piece;
  isSelected?: boolean;
  onClick?: () => void;
  cellSize: number;
}

export const ChessPiece: React.FC<ChessPieceProps> = ({ piece, isSelected, onClick, cellSize }) => {
  const isRed = piece.color === PlayerColor.RED;
  const label = getPieceLabel(piece);
  
  // Calculate center position
  const left = piece.position.x * cellSize;
  const top = piece.position.y * cellSize;

  return (
    <div
      className={`chess-piece ${isRed ? 'red' : 'black'} ${isSelected ? 'selected' : ''}`}
      style={{
        width: cellSize * 0.8,
        height: cellSize * 0.8,
        left: left + cellSize * 0.1,
        top: top + cellSize * 0.1,
        transform: `translate(0, 0)`, // For potential future transition handling
      }}
      onClick={(e) => {
        e.stopPropagation();
        if (onClick) onClick();
      }}
    >
      <div className="piece-inner">
        {label}
      </div>
    </div>
  );
};
