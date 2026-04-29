import { ChessBoardCanvas } from './components/ChessBoardCanvas';
import { useGame, GameMode } from './hooks/useGame';
import './App.css';
import { PlayerColor } from './models/types';

function App() {
  const {
    board,
    currentPlayer,
    isGameOver,
    winner,
    victoryType,
    selectedPiece,
    possibleMoves,
    handleSquareClick,
    resetGame,
    handleSave,
    handleLoad,
    hasSave,
    gameMode,
    setGameMode,
    difficulty,
    setDifficulty,
    isThinking,
    inCheckAlert,
  } = useGame();

  return (
    <div className="app-container">
      {/* 水墨风格结算界面 */}
      {isGameOver && (
        <div className="ink-overlay">
          <div className="ink-content">
            <h1 className={"ink-title " + (winner === PlayerColor.RED ? "red-win" : "black-win")}>
              {winner === PlayerColor.RED ? '红方大捷' : '黑方大捷'}
            </h1>
            <h2 className="ink-subtitle">{victoryType}</h2>
            <button className="ink-btn" onClick={resetGame}>再弈一局</button>
          </div>
        </div>
      )}

      {/* 顶部状态栏 */}
      <header className="top-bar">
        <div className="top-bar-left">
          <h1 className="title">象棋</h1>
        </div>
        <div className="top-bar-center">
          {isGameOver ? (
            <span className="status-text game-over-text">大局已定</span>
          ) : (
            <>
              <span className={`turn-dot ${currentPlayer === PlayerColor.RED ? 'red-dot' : 'black-dot'}`} />
              <span className="status-text">
                {currentPlayer === PlayerColor.RED ? '红方' : '黑方'}行棋
              </span>
              {isThinking && <span className="thinking-indicator" />}
            </>
          )}
        </div>
        <div className="top-bar-right">
          {inCheckAlert && !isGameOver && (
            <span className="check-badge pulse-strong">将军</span>
          )}
        </div>
      </header>

      {/* 棋盘区域 - 尽可能大 */}
      <main className="board-area">
        <ChessBoardCanvas
          board={board}
          selectedPiece={selectedPiece}
          possibleMoves={possibleMoves}
          onSquareClick={handleSquareClick}
          isThinking={isThinking}
        />
      </main>

      {/* 底部工具栏 */}
      <footer className="bottom-bar">
        <div className="toolbar-row">
          <select className="mode-select" value={gameMode} onChange={e => setGameMode(e.target.value as GameMode)}>
            <option value={GameMode.PVE}>人机</option>
            <option value={GameMode.PVP}>双人</option>
          </select>

          {gameMode === GameMode.PVE && (
            <select className="mode-select" value={difficulty} onChange={e => setDifficulty(Number(e.target.value))}>
              <option value={2}>初级</option>
              <option value={3}>中级</option>
              <option value={4}>高级</option>
            </select>
          )}

          <button className="tool-btn" onClick={handleSave} disabled={isGameOver} title="存档">
            💾
          </button>
          <button className="tool-btn" onClick={handleLoad} disabled={!hasSave} title="读档">
            📂
          </button>
          <button className="tool-btn restart-btn" onClick={resetGame} title="重新开始">
            🔄
          </button>
        </div>
      </footer>
    </div>
  );
}

export default App;
