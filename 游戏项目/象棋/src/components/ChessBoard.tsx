import React, { useRef, useEffect, useState } from 'react';
import type { BoardState, Position, Piece } from '../models/types';
import { ChessPiece } from './ChessPiece';
import './ChessBoard.css';

interface ChessBoardProps {
  board: BoardState;
  onSquareClick: (x: number, y: number) => void;
  selectedPiece: Piece | null;
  possibleMoves: Position[];
}

export const ChessBoard: React.FC<ChessBoardProps> = ({ board, onSquareClick, selectedPiece, possibleMoves }) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const [cellSize, setCellSize] = useState(50);

  // Resize logic to make it responsive
  useEffect(() => {
    const updateSize = () => {
      if (containerRef.current) {
        // We have 9 cols, so 8 cells horizontally. The grid lines go from center of piece to center.
        // The container needs to fit 9 columns of pieces.
        const width = containerRef.current.clientWidth;
        setCellSize(width / 9);
      }
    };
    updateSize();
    window.addEventListener('resize', updateSize);
    return () => window.removeEventListener('resize', updateSize);
  }, []);

  const drawGrid = () => {
    // 渲染棋盘的线条，楚河汉界等
    return (
      <div className="grid-bg">
        {/* Horizontal lines */}
        {Array.from({ length: 10 }).map((_, i) => (
          <div key={`h-${i}`} className="grid-line h-line" style={{ top: `${i * cellSize + cellSize / 2}px`, left: `${cellSize / 2}px`, width: `${8 * cellSize}px` }} />
        ))}
        {/* Vertical lines (Top half) */}
        {Array.from({ length: 9 }).map((_, i) => (
          <div key={`vt-${i}`} className="grid-line v-line" style={{ left: `${i * cellSize + cellSize / 2}px`, top: `${cellSize / 2}px`, height: `${4 * cellSize}px` }} />
        ))}
        {/* Vertical lines (Bottom half) */}
        {Array.from({ length: 9 }).map((_, i) => (
          <div key={`vb-${i}`} className="grid-line v-line" style={{ left: `${i * cellSize + cellSize / 2}px`, top: `${5 * cellSize + cellSize / 2}px`, height: `${4 * cellSize}px` }} />
        ))}
        
        {/* 楚河汉界文字区 */}
        <div className="river" style={{ top: `${4 * cellSize + cellSize/2}px`, height: `${cellSize}px`, left: `${cellSize/2}px`, width: `${8 * cellSize}px` }}>
          <span>楚河</span>
          <span>汉界</span>
        </div>

        {/* 九宫格斜线 顶部 */}
        <div className="diag-line" style={{ top: `${cellSize/2}px`, left: `${3 * cellSize + cellSize/2}px`, width: `${Math.sqrt(8)*cellSize}px`, transformOrigin: 'top left', transform: 'rotate(45deg)' }} />
        <div className="diag-line" style={{ top: `${cellSize/2}px`, left: `${5 * cellSize + cellSize/2}px`, width: `${Math.sqrt(8)*cellSize}px`, transformOrigin: 'top left', transform: 'rotate(135deg)' }} />
        
        {/* 九宫格斜线 底部 */}
        <div className="diag-line" style={{ top: `${7 * cellSize + cellSize/2}px`, left: `${3 * cellSize + cellSize/2}px`, width: `${Math.sqrt(8)*cellSize}px`, transformOrigin: 'top left', transform: 'rotate(45deg)' }} />
        <div className="diag-line" style={{ top: `${7 * cellSize + cellSize/2}px`, left: `${5 * cellSize + cellSize/2}px`, width: `${Math.sqrt(8)*cellSize}px`, transformOrigin: 'top left', transform: 'rotate(135deg)' }} />
        
        {/* 落子指示器 (星星符号等，这里为了简洁先省略详细的兵站炮台标记) */}
      </div>
    );
  };

  const renderPieces = () => {
    const pieces: React.ReactNode[] = [];
    for (let y = 0; y < 10; y++) {
      for (let x = 0; x < 9; x++) {
        const piece = board[y][x];
        if (piece) {
          pieces.push(
            <ChessPiece
              key={piece.id}
              piece={piece}
              isSelected={selectedPiece?.id === piece.id}
              cellSize={cellSize}
              onClick={() => onSquareClick(x, y)}
            />
          );
        }
      }
    }
    return pieces;
  };

  const renderHighlights = () => {
    return possibleMoves.map((pos, idx) => (
      <div
        key={`hl-${pos.x}-${pos.y}-${idx}`}
        className="highlight-dot"
        style={{
          left: pos.x * cellSize + cellSize * 0.5,
          top: pos.y * cellSize + cellSize * 0.5,
        }}
        onClick={(e) => {
          e.stopPropagation();
          onSquareClick(pos.x, pos.y);
        }}
      />
    ));
  };

  return (
    <div 
      className="chess-board-container" 
      ref={containerRef}
      style={{ '--cell-size': `${cellSize}px` } as React.CSSProperties}
      onClick={(e) => {
        const rect = containerRef.current?.getBoundingClientRect();
        if (rect) {
          const x = Math.floor((e.clientX - rect.left) / cellSize);
          const y = Math.floor((e.clientY - rect.top) / cellSize);
          if (x >= 0 && x <= 8 && y >= 0 && y <= 9) {
            onSquareClick(x, y);
          }
        }
      }}
    >
      <div className="wood-texture" />
      {drawGrid()}
      {renderHighlights()}
      {renderPieces()}
    </div>
  );
};
