import { useRef, useEffect, useCallback } from 'react';
import { PlayerColor, getPieceLabel } from '../models/types';
import type { BoardState, Piece, Position } from '../models/types';

interface ChessBoardCanvasProps {
  board: BoardState;
  onSquareClick: (x: number, y: number) => void;
  selectedPiece: Piece | null;
  possibleMoves: Position[];
  isThinking: boolean;
}

// ============================================================
//  经典色彩主题
// ============================================================
const COLORS = {
  boardBg: '#E8C97A',       // 棋盘底色(黄木)
  boardLine: '#4A3728',     // 棋盘线条(深褐)
  borderOuter: '#6B4226',   // 外边框
  borderInner: '#8B5E3C',   // 内边框
  pieceBase: '#FAEBD7',     // 棋子底色-米白
  pieceBorder: '#8B4513',   // 棋子边框
  pieceRedText: '#CC0000',  // 红方字
  pieceBlackText: '#1A1A1A',// 黑方字
  selectedGlow: '#00FF00',  // 选中光晕
  moveHint: 'rgba(0,200,0,0.55)', // 可走位提示
  lastMove: 'rgba(255,200,0,0.35)', // 上一步高亮
};

export const ChessBoardCanvas: React.FC<ChessBoardCanvasProps> = ({
  board, onSquareClick, selectedPiece, possibleMoves, isThinking
}) => {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  const layoutRef = useRef({ cellSize: 0, padding: 0 });

  // 计算棋盘尺寸 - 9列8间隔, 10行9间隔
  const COLS = 9;
  const ROWS = 10;

  const draw = useCallback(() => {
    const canvas = canvasRef.current;
    const container = containerRef.current;
    if (!canvas || !container) return;

    const dpr = window.devicePixelRatio || 1;
    const containerWidth = container.clientWidth;
    const containerHeight = container.clientHeight;

    // 根据容器的宽高比决定 cellSize：宽度需要放 COLS+1 格，高度需要放 ROWS+1 格
    const cellByWidth = Math.floor(containerWidth / (COLS + 1));
    const cellByHeight = Math.floor(containerHeight / (ROWS + 1.2));
    const cellSize = Math.min(cellByWidth, cellByHeight);

    const padding = cellSize * 0.65;

    const canvasW = cellSize * (COLS - 1) + padding * 2;
    const canvasH = cellSize * (ROWS - 1) + padding * 2;

    canvas.width = canvasW * dpr;
    canvas.height = canvasH * dpr;
    canvas.style.width = `${canvasW}px`;
    canvas.style.height = `${canvasH}px`;

    const ctx = canvas.getContext('2d')!;
    ctx.scale(dpr, dpr);

    // 存储给click/touch回调使用的布局参数
    layoutRef.current = { cellSize, padding };

    const pieceRadius = cellSize * 0.42;

    // --------- 1. 绘制木纹棋盘底色 ---------
    ctx.fillStyle = COLORS.borderOuter;
    ctx.fillRect(0, 0, canvasW, canvasH);

    const borderW = Math.max(4, cellSize * 0.08);
    ctx.fillStyle = COLORS.borderInner;
    ctx.fillRect(borderW, borderW, canvasW - borderW * 2, canvasH - borderW * 2);

    const boardLeft = padding - cellSize * 0.1;
    const boardTop = padding - cellSize * 0.1;
    const boardW = cellSize * (COLS - 1) + cellSize * 0.2;
    const boardH = cellSize * (ROWS - 1) + cellSize * 0.2;
    ctx.fillStyle = COLORS.boardBg;
    ctx.fillRect(boardLeft, boardTop, boardW, boardH);

    // 木纹效果
    ctx.save();
    ctx.globalAlpha = 0.06;
    for (let i = 0; i < 40; i++) {
      ctx.strokeStyle = '#8B6914';
      ctx.lineWidth = Math.random() * 2 + 0.5;
      ctx.beginPath();
      const yy = Math.random() * canvasH;
      ctx.moveTo(0, yy);
      ctx.bezierCurveTo(canvasW * 0.3, yy + (Math.random() - 0.5) * 15, canvasW * 0.7, yy + (Math.random() - 0.5) * 15, canvasW, yy);
      ctx.stroke();
    }
    ctx.restore();

    // --------- 2. 绘制棋盘网格线 ---------
    ctx.strokeStyle = COLORS.boardLine;
    ctx.lineWidth = 1.2;

    for (let row = 0; row < ROWS; row++) {
      ctx.beginPath();
      ctx.moveTo(padding, padding + row * cellSize);
      ctx.lineTo(padding + (COLS - 1) * cellSize, padding + row * cellSize);
      ctx.stroke();
    }

    for (let col = 0; col < COLS; col++) {
      ctx.beginPath();
      ctx.moveTo(padding + col * cellSize, padding);
      ctx.lineTo(padding + col * cellSize, padding + 4 * cellSize);
      ctx.stroke();
      ctx.beginPath();
      ctx.moveTo(padding + col * cellSize, padding + 5 * cellSize);
      ctx.lineTo(padding + col * cellSize, padding + 9 * cellSize);
      ctx.stroke();
    }
    ctx.beginPath();
    ctx.moveTo(padding, padding + 4 * cellSize);
    ctx.lineTo(padding, padding + 5 * cellSize);
    ctx.stroke();
    ctx.beginPath();
    ctx.moveTo(padding + 8 * cellSize, padding + 4 * cellSize);
    ctx.lineTo(padding + 8 * cellSize, padding + 5 * cellSize);
    ctx.stroke();

    // --------- 3. 九宫斜线 ---------
    ctx.lineWidth = 1;
    ctx.beginPath(); ctx.moveTo(padding + 3 * cellSize, padding); ctx.lineTo(padding + 5 * cellSize, padding + 2 * cellSize); ctx.stroke();
    ctx.beginPath(); ctx.moveTo(padding + 5 * cellSize, padding); ctx.lineTo(padding + 3 * cellSize, padding + 2 * cellSize); ctx.stroke();
    ctx.beginPath(); ctx.moveTo(padding + 3 * cellSize, padding + 7 * cellSize); ctx.lineTo(padding + 5 * cellSize, padding + 9 * cellSize); ctx.stroke();
    ctx.beginPath(); ctx.moveTo(padding + 5 * cellSize, padding + 7 * cellSize); ctx.lineTo(padding + 3 * cellSize, padding + 9 * cellSize); ctx.stroke();

    // --------- 4.「楚河 汉界」 ---------
    ctx.save();
    ctx.font = `bold ${cellSize * 0.5}px "KaiTi", "STKaiti", serif`;
    ctx.fillStyle = COLORS.boardLine;
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    const riverY = padding + 4.5 * cellSize;
    ctx.fillText('楚  河', padding + 2 * cellSize, riverY);
    ctx.fillText('汉  界', padding + 6 * cellSize, riverY);
    ctx.restore();

    // --------- 5. 星位标记 ---------
    const drawCross = (cx: number, cy: number, hasLeft: boolean, hasRight: boolean) => {
      const s = cellSize * 0.07;
      const gap = cellSize * 0.05;
      ctx.lineWidth = 1;
      ctx.strokeStyle = COLORS.boardLine;
      if (hasLeft) {
        ctx.beginPath(); ctx.moveTo(cx - gap - s, cy - gap); ctx.lineTo(cx - gap, cy - gap); ctx.lineTo(cx - gap, cy - gap - s); ctx.stroke();
        ctx.beginPath(); ctx.moveTo(cx - gap - s, cy + gap); ctx.lineTo(cx - gap, cy + gap); ctx.lineTo(cx - gap, cy + gap + s); ctx.stroke();
      }
      if (hasRight) {
        ctx.beginPath(); ctx.moveTo(cx + gap + s, cy - gap); ctx.lineTo(cx + gap, cy - gap); ctx.lineTo(cx + gap, cy - gap - s); ctx.stroke();
        ctx.beginPath(); ctx.moveTo(cx + gap + s, cy + gap); ctx.lineTo(cx + gap, cy + gap); ctx.lineTo(cx + gap, cy + gap + s); ctx.stroke();
      }
    };
    drawCross(padding + 1 * cellSize, padding + 2 * cellSize, false, true);
    drawCross(padding + 7 * cellSize, padding + 2 * cellSize, true, false);
    drawCross(padding + 1 * cellSize, padding + 7 * cellSize, false, true);
    drawCross(padding + 7 * cellSize, padding + 7 * cellSize, true, false);
    for (let i = 0; i < 5; i++) {
      const bx = padding + i * 2 * cellSize;
      drawCross(bx, padding + 3 * cellSize, i > 0, i < 4);
      drawCross(bx, padding + 6 * cellSize, i > 0, i < 4);
    }

    // --------- 6. 选中与可走位 ---------
    if (selectedPiece) {
      const sx = padding + selectedPiece.position.x * cellSize;
      const sy = padding + selectedPiece.position.y * cellSize;
      ctx.save();
      ctx.strokeStyle = COLORS.selectedGlow;
      ctx.lineWidth = 2.5;
      ctx.shadowColor = COLORS.selectedGlow;
      ctx.shadowBlur = 12;
      ctx.beginPath();
      ctx.arc(sx, sy, pieceRadius + 2, 0, Math.PI * 2);
      ctx.stroke();
      ctx.restore();
    }

    possibleMoves.forEach(pos => {
      const mx = padding + pos.x * cellSize;
      const my = padding + pos.y * cellSize;
      if (board[pos.y][pos.x]) {
        ctx.save();
        ctx.strokeStyle = 'rgba(255,60,60,0.7)';
        ctx.lineWidth = 2.5;
        ctx.beginPath();
        ctx.arc(mx, my, pieceRadius + 2, 0, Math.PI * 2);
        ctx.stroke();
        ctx.restore();
      } else {
        ctx.save();
        ctx.fillStyle = COLORS.moveHint;
        ctx.beginPath();
        ctx.arc(mx, my, cellSize * 0.1, 0, Math.PI * 2);
        ctx.fill();
        ctx.restore();
      }
    });

    // --------- 7. 棋子 ---------
    for (let y = 0; y < ROWS; y++) {
      for (let x = 0; x < COLS; x++) {
        const piece = board[y][x];
        if (!piece) continue;

        const cx = padding + x * cellSize;
        const cy = padding + y * cellSize;
        const isRed = piece.color === PlayerColor.RED;
        const label = getPieceLabel(piece);

        // 柔和的坠落投影 (3D)
        ctx.save();
        ctx.fillStyle = 'rgba(0,0,0,0.35)';
        ctx.shadowColor = 'rgba(0,0,0,0.4)';
        ctx.shadowBlur = 4;
        ctx.shadowOffsetY = 3;
        ctx.beginPath();
        ctx.arc(cx, cy + 2, pieceRadius, 0, Math.PI * 2);
        ctx.fill();
        ctx.restore();

        // 立体材质渐变
        ctx.save();
        const grad = ctx.createRadialGradient(cx - pieceRadius * 0.35, cy - pieceRadius * 0.35, pieceRadius * 0.05, cx, cy, pieceRadius * 1.1);
        grad.addColorStop(0, '#FFFFFF');
        grad.addColorStop(0.3, '#FFF3DA');
        grad.addColorStop(0.85, '#DAB36E');
        grad.addColorStop(1, '#A07635');
        ctx.fillStyle = grad;
        ctx.beginPath();
        ctx.arc(cx, cy, pieceRadius, 0, Math.PI * 2);
        ctx.fill();
        
        ctx.strokeStyle = '#613813';
        ctx.lineWidth = 1.5;
        ctx.stroke();
        ctx.restore();

        // 内圈刻线
        ctx.save();
        ctx.strokeStyle = isRed ? 'rgba(204,0,0,0.4)' : 'rgba(26,26,26,0.3)';
        ctx.lineWidth = 1;
        ctx.beginPath();
        ctx.arc(cx, cy, pieceRadius * 0.78, 0, Math.PI * 2);
        ctx.stroke();
        ctx.restore();

        // 雕刻文字（增加内发光与描边，更具有立体刻字的效果）
        ctx.save();
        ctx.font = `bold ${pieceRadius * 1.15}px "KaiTi", "STKaiti", "SimSun", serif`;
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        
        // 字的内凹/外凸阴影效果
        ctx.shadowColor = 'rgba(255,255,255,0.7)';
        ctx.shadowBlur = 1;
        ctx.shadowOffsetY = 1;
        ctx.fillStyle = isRed ? '#AA0000' : '#111111';
        
        ctx.fillText(label, cx, cy + 1);
        ctx.restore();
      }
    }

  }, [board, selectedPiece, possibleMoves, isThinking, COLS, ROWS]);

  useEffect(() => { draw(); }, [draw]);

  useEffect(() => {
    const handleResize = () => draw();
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, [draw]);

  // 统一处理 click 和 touch
  const getGridPos = useCallback((clientX: number, clientY: number) => {
    const canvas = canvasRef.current;
    if (!canvas) return null;
    const rect = canvas.getBoundingClientRect();
    const { cellSize, padding } = layoutRef.current;
    if (!cellSize) return null;

    const x = clientX - rect.left;
    const y = clientY - rect.top;
    const col = Math.round((x - padding) / cellSize);
    const row = Math.round((y - padding) / cellSize);

    if (col >= 0 && col <= 8 && row >= 0 && row <= 9) {
      return { col, row };
    }
    return null;
  }, []);

  const handleClick = useCallback((e: React.MouseEvent<HTMLCanvasElement>) => {
    const pos = getGridPos(e.clientX, e.clientY);
    if (pos) onSquareClick(pos.col, pos.row);
  }, [onSquareClick, getGridPos]);

  const handleTouch = useCallback((e: React.TouchEvent<HTMLCanvasElement>) => {
    e.preventDefault(); // 防止双击缩放
    const touch = e.changedTouches[0];
    if (touch) {
      const pos = getGridPos(touch.clientX, touch.clientY);
      if (pos) onSquareClick(pos.col, pos.row);
    }
  }, [onSquareClick, getGridPos]);

  return (
    <div ref={containerRef} style={{ width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
      <canvas
        ref={canvasRef}
        onClick={handleClick}
        onTouchEnd={handleTouch}
        style={{ display: 'block', cursor: 'pointer', borderRadius: '4px', touchAction: 'none' }}
      />
    </div>
  );
};
