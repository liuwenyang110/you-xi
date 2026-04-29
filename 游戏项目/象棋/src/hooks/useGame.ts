import { useState, useCallback, useEffect } from 'react';
import { GameManager } from '../logic/GameManager';
import { createInitialBoard } from '../logic/Board';
import { PlayerColor } from '../models/types';
import type { Piece, Position, BoardState } from '../models/types';
import { getPossibleMoves, isKingInCheck } from '../logic/Rules';
import { getBestMove, resetAIState } from '../logic/AI';
import { audioEngine } from '../utils/audio';
import { saveGame, loadGame, hasSavedGame, deleteSave } from '../utils/storage';

export const GameMode = {
  PVP: 'PVP',
  PVE: 'PVE',
} as const;
export type GameMode = typeof GameMode[keyof typeof GameMode];

export const useGame = () => {
  const [manager, setManager] = useState(() => new GameManager(createInitialBoard()));
  const [board, setBoard] = useState<BoardState>(manager.board);
  const [selectedPiece, setSelectedPiece] = useState<Piece | null>(null);
  const [possibleMoves, setPossibleMoves] = useState<Position[]>([]);
  const [gameMode, setGameMode] = useState<GameMode>(GameMode.PVE);
  const [difficulty, setDifficulty] = useState<number>(3);
  const [isThinking, setIsThinking] = useState(false);
  const [inCheckAlert, setInCheckAlert] = useState(false);

  const syncState = useCallback(() => {
    setBoard([...manager.board.map(row => [...row])]);
  }, [manager]);

  const selectPiece = useCallback((piece: Piece) => {
    setSelectedPiece(piece);
    // 只展示经过禁手过滤的合法走子（不能送将、不能将帅照面）
    const rawMoves = getPossibleMoves(manager.board, piece);
    setPossibleMoves(rawMoves);
  }, [manager]);

  const clearSelection = useCallback(() => {
    setSelectedPiece(null);
    setPossibleMoves([]);
  }, []);

  const handleSquareClick = useCallback((x: number, y: number) => {
    if (manager.isGameOver || isThinking) return;

    const clickedPiece = manager.board[y]?.[x] ?? null;

    if (selectedPiece) {
      if (clickedPiece && clickedPiece.color === selectedPiece.color) {
        selectPiece(clickedPiece);
      } else {
        const targetOccupied = manager.board[y][x] !== null;
        const success = manager.tryMove(selectedPiece, x, y);
        if (success) {
          audioEngine.init();
          
          if (manager.isGameOver) {
            audioEngine.playGameOver();
            setInCheckAlert(false);
          } else {
            const check = isKingInCheck(manager.board, manager.currentPlayer);
            if (check) {
              audioEngine.playCheck();
              setInCheckAlert(true);
            } else if (targetOccupied) {
              audioEngine.playCapture();
              setInCheckAlert(false);
            } else {
              audioEngine.playMove();
              setInCheckAlert(false);
            }
          }
          clearSelection();
          syncState();
        } else {
          clearSelection();
        }
      }
    } else {
      if (clickedPiece && clickedPiece.color === manager.currentPlayer) {
        if (gameMode === GameMode.PVE && manager.currentPlayer === PlayerColor.BLACK) {
          return;
        }
        selectPiece(clickedPiece);
      }
    }
  }, [manager, selectedPiece, isThinking, gameMode, syncState, selectPiece, clearSelection]);

  // AI 回合
  useEffect(() => {
    let isActive = true; // 防泄漏与滞后更新标记

    if (gameMode === GameMode.PVE && !manager.isGameOver && manager.currentPlayer === PlayerColor.BLACK) {
      setIsThinking(true);
      // 延迟 800ms 给玩家看清上一步
      const timer = setTimeout(() => {
        if (!isActive) return;

        const bestMove = getBestMove(manager.board, difficulty);
        if (bestMove) {
          const piece = manager.board[bestMove.f.y][bestMove.f.x];
          if (piece) {
            const targetOccupied = manager.board[bestMove.t.y][bestMove.t.x] !== null;
            manager.tryMove(piece, bestMove.t.x, bestMove.t.y);
            
            audioEngine.init();
            if (manager.isGameOver) {
               audioEngine.playGameOver();
               setInCheckAlert(false);
            } else {
               const check = isKingInCheck(manager.board, manager.currentPlayer);
               if (check) {
                 audioEngine.playCheck();
                 setInCheckAlert(true);
               } else if (targetOccupied) {
                 audioEngine.playCapture();
                 setInCheckAlert(false);
               } else {
                 audioEngine.playMove();
                 setInCheckAlert(false);
               }
            }
            syncState();
          }
        }
        if (isActive) setIsThinking(false);
      }, 800);
      return () => {
        isActive = false;
        clearTimeout(timer);
      };
    }
  }, [manager.currentPlayer, gameMode, difficulty, manager, syncState]);

  const resetGame = useCallback(() => {
    const newManager = new GameManager(createInitialBoard());
    setManager(newManager);
    setBoard(newManager.board);
    setSelectedPiece(null);
    setPossibleMoves([]);
    setInCheckAlert(false);
    setIsThinking(false);
    deleteSave();
    resetAIState();
  }, []);

  const handleSave = useCallback(() => {
    if (!manager.isGameOver) {
      saveGame(manager.board, manager.currentPlayer);
    }
  }, [manager]);

  const handleLoad = useCallback(() => {
    const data = loadGame();
    if (data) {
      const newManager = new GameManager(data.board);
      newManager.currentPlayer = data.currentPlayer;
      setManager(newManager);
      setBoard(newManager.board);
      setSelectedPiece(null);
      setPossibleMoves([]);
      setInCheckAlert(false);
      setIsThinking(false);
    }
  }, []);

  return {
    board,
    currentPlayer: manager.currentPlayer,
    isGameOver: manager.isGameOver,
    winner: manager.winner,
    victoryType: manager.victoryType,
    selectedPiece,
    possibleMoves,
    handleSquareClick,
    resetGame,
    handleSave,
    handleLoad,
    hasSave: hasSavedGame(),
    gameMode,
    setGameMode,
    difficulty,
    setDifficulty,
    isThinking,
    inCheckAlert,
  };
};
