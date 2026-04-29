import type { BoardState } from '../models/types';
import { PlayerColor } from '../models/types';

const SAVE_KEY = 'xiangqi_save';

export interface SaveData {
  board: BoardState;
  currentPlayer: PlayerColor;
  savedAt: string;
}

export const saveGame = (board: BoardState, currentPlayer: PlayerColor): void => {
  const data: SaveData = {
    board,
    currentPlayer,
    savedAt: new Date().toLocaleString('zh-CN'),
  };
  localStorage.setItem(SAVE_KEY, JSON.stringify(data));
};

export const loadGame = (): SaveData | null => {
  const raw = localStorage.getItem(SAVE_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as SaveData;
  } catch {
    return null;
  }
};

export const hasSavedGame = (): boolean => {
  return localStorage.getItem(SAVE_KEY) !== null;
};

export const deleteSave = (): void => {
  localStorage.removeItem(SAVE_KEY);
};
