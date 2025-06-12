// static/js/games/snakeGame.js
class SnakeGame {
    constructor() {
        this.canvas = null;
        this.ctx = null;
        this.gameArea = null;

        // 게임 설정
        this.gridSize = 20;
        this.canvasSize = 320;
        this.cellSize = this.canvasSize / this.gridSize;

        // 게임 상태
        this.snake = [];
        this.direction = { x: 1, y: 0 };
        this.food = { x: 0, y: 0 };
        this.score = 0;
        this.isRunning = false;
        this.gameLoop = null;
        this.speed = 150; // ms

        // 키보드 이벤트 핸들러
        this.keyHandler = null;
    }

    init(gameArea) {
        this.gameArea = gameArea;
        this.setupUI();
        this.setupCanvas();
        this.setupControls();
        this.resetGame();
        return this;
    }

    setupUI() {
        this.gameArea.innerHTML = `
            <div class="snake-game-container">
                <div class="snake-header">
                    <div id="snake-score" class="snake-score">점수: 0</div>
                    <div class="snake-high-score" id="snake-high-score">최고점수: ${this.getHighScore()}</div>
                </div>

                <div class="snake-canvas-container">
                    <canvas id="snake-canvas" class="snake-canvas"></canvas>
                    <div id="snake-game-over" class="snake-game-over hidden">
                        <div class="game-over-content">
                            <div class="game-over-title">🐍 게임 오버!</div>
                            <div id="game-over-score" class="game-over-score"></div>
                            <div id="game-over-grade" class="game-over-grade"></div>
                            <div id="game-over-message" class="game-over-message"></div>
                        </div>
                    </div>
                </div>

                <div class="snake-controls">
                    <div class="control-instructions">
                        방향키로 조작하거나 터치로 스와이프하세요!
                    </div>
                    <div class="control-buttons">
                        <button id="snake-start-btn" class="game-btn primary">🚀 시작</button>
                        <button id="snake-reset-btn" class="game-btn secondary">🔄 다시하기</button>
                    </div>
                </div>

                <!-- 모바일용 방향 패드 -->
                <div class="direction-pad">
                    <div class="dpad-row">
                        <button class="dpad-btn" data-direction="up">↑</button>
                    </div>
                    <div class="dpad-row">
                        <button class="dpad-btn" data-direction="left">←</button>
                        <button class="dpad-btn center"></button>
                        <button class="dpad-btn" data-direction="right">→</button>
                    </div>
                    <div class="dpad-row">
                        <button class="dpad-btn" data-direction="down">↓</button>
                    </div>
                </div>
            </div>
        `;

        this.addStyles();
        this.setupEventListeners();
    }

    addStyles() {
        const style = document.createElement('style');
        style.textContent = `
            .snake-game-container {
                display: flex;
                flex-direction: column;
                align-items: center;
                gap: 15px;
                user-select: none;
            }

            .snake-header {
                display: flex;
                justify-content: space-between;
                width: 100%;
                max-width: 320px;
                padding: 0 10px;
            }

            .snake-score, .snake-high-score {
                color: #fff;
                font-size: 1.1rem;
                font-weight: bold;
            }

            .snake-high-score {
                color: #ffd700;
            }

            .snake-canvas-container {
                position: relative;
                border-radius: 10px;
                overflow: hidden;
                box-shadow: 0 4px 15px rgba(0,0,0,0.3);
            }

            .snake-canvas {
                display: block;
                background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
                border: 2px solid #7461ee;
            }

            .snake-game-over {
                position: absolute;
                top: 0;
                left: 0;
                right: 0;
                bottom: 0;
                background: rgba(0, 0, 0, 0.9);
                display: flex;
                align-items: center;
                justify-content: center;
                backdrop-filter: blur(5px);
            }

            .snake-game-over.hidden {
                display: none;
            }

            .game-over-content {
                text-align: center;
                color: white;
                padding: 20px;
            }

            .game-over-title {
                font-size: 1.8rem;
                font-weight: bold;
                margin-bottom: 15px;
                color: #ff6b6b;
            }

            .game-over-score {
                font-size: 1.3rem;
                margin-bottom: 10px;
                color: #4ecdc4;
            }

            .game-over-grade {
                font-size: 1.1rem;
                margin-bottom: 15px;
                padding: 8px 16px;
                border-radius: 20px;
                display: inline-block;
                font-weight: bold;
            }

            .game-over-message {
                font-size: 1rem;
                line-height: 1.4;
                color: #ccc;
            }

            .snake-controls {
                text-align: center;
            }

            .control-instructions {
                color: #ccc;
                font-size: 0.9rem;
                margin-bottom: 15px;
            }

            .control-buttons {
                display: flex;
                gap: 10px;
                justify-content: center;
                flex-wrap: wrap;
            }

            .game-btn {
                padding: 10px 20px;
                border: none;
                border-radius: 20px;
                font-size: 0.9rem;
                font-weight: 500;
                cursor: pointer;
                transition: all 0.3s ease;
                min-width: 100px;
            }

            .game-btn.primary {
                background: linear-gradient(45deg, #7461ee, #ff6b9d);
                color: white;
            }

            .game-btn.primary:hover:not(:disabled) {
                transform: translateY(-2px);
                box-shadow: 0 6px 15px rgba(116, 97, 238, 0.4);
            }

            .game-btn.secondary {
                background: rgba(255, 255, 255, 0.1);
                color: white;
                border: 1px solid rgba(255, 255, 255, 0.2);
            }

            .game-btn.secondary:hover:not(:disabled) {
                background: rgba(255, 255, 255, 0.2);
                transform: translateY(-2px);
            }

            .game-btn:disabled {
                opacity: 0.5;
                cursor: not-allowed;
                transform: none !important;
            }

            .game-btn.hidden {
                display: none !important;
            }

            /* 모바일용 방향 패드 */
            .direction-pad {
                display: grid;
                grid-template-rows: repeat(3, 1fr);
                gap: 5px;
                margin-top: 15px;
            }

            .dpad-row {
                display: grid;
                grid-template-columns: repeat(3, 1fr);
                gap: 5px;
            }

            .dpad-btn {
                width: 50px;
                height: 50px;
                border: 2px solid rgba(255, 255, 255, 0.3);
                background: rgba(255, 255, 255, 0.1);
                color: white;
                border-radius: 8px;
                font-size: 1.2rem;
                cursor: pointer;
                transition: all 0.2s ease;
                display: flex;
                align-items: center;
                justify-content: center;
            }

            .dpad-btn:not(.center):hover {
                background: rgba(116, 97, 238, 0.5);
                border-color: #7461ee;
            }

            .dpad-btn:not(.center):active {
                transform: scale(0.95);
            }

            .dpad-btn.center {
                background: transparent;
                border: none;
                cursor: default;
            }

            /* 데스크톱에서는 방향패드 숨김 */
            @media (min-width: 768px) {
                .direction-pad {
                    display: none;
                }
            }

            /* 등급별 색상 */
            .grade-s-plus { background: linear-gradient(45deg, #ffd700, #ffed4e); color: #333; }
            .grade-s { background: linear-gradient(45deg, #c0c0c0, #e8e8e8); color: #333; }
            .grade-a { background: linear-gradient(45deg, #cd7f32, #daa520); color: white; }
            .grade-b { background: linear-gradient(45deg, #4ecdc4, #44a08d); color: white; }
            .grade-c { background: linear-gradient(45deg, #95a5a6, #7f8c8d); color: white; }
            .grade-d { background: linear-gradient(45deg, #e74c3c, #c0392b); color: white; }
        `;

        if (!document.getElementById('snake-game-styles')) {
            style.id = 'snake-game-styles';
            document.head.appendChild(style);
        }
    }

    setupCanvas() {
        this.canvas = document.getElementById('snake-canvas');
        this.canvas.width = this.canvasSize;
        this.canvas.height = this.canvasSize;
        this.ctx = this.canvas.getContext('2d');
    }

    setupEventListeners() {
        // 게임 제어 버튼
        document.getElementById('snake-start-btn').addEventListener('click', () => this.startGame());
        document.getElementById('snake-reset-btn').addEventListener('click', () => this.resetGame());

        // 방향 패드 (모바일)
        document.querySelectorAll('.dpad-btn[data-direction]').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const direction = e.target.dataset.direction;
                this.changeDirection(direction);
            });
        });
    }

    setupControls() {
        // 키보드 이벤트 핸들러
        this.keyHandler = (e) => {
            if (!this.isRunning) return;

            switch(e.key) {
                case 'ArrowUp':
                case 'w':
                case 'W':
                    this.changeDirection('up');
                    break;
                case 'ArrowDown':
                case 's':
                case 'S':
                    this.changeDirection('down');
                    break;
                case 'ArrowLeft':
                case 'a':
                case 'A':
                    this.changeDirection('left');
                    break;
                case 'ArrowRight':
                case 'd':
                case 'D':
                    this.changeDirection('right');
                    break;
            }
            e.preventDefault();
        };

        document.addEventListener('keydown', this.keyHandler);

        // 터치 스와이프 지원
        this.setupTouchControls();
    }

    setupTouchControls() {
        let startX, startY;

        this.canvas.addEventListener('touchstart', (e) => {
            const touch = e.touches[0];
            startX = touch.clientX;
            startY = touch.clientY;
            e.preventDefault();
        });

        this.canvas.addEventListener('touchend', (e) => {
            if (!startX || !startY) return;

            const touch = e.changedTouches[0];
            const diffX = touch.clientX - startX;
            const diffY = touch.clientY - startY;

            const threshold = 30;

            if (Math.abs(diffX) > Math.abs(diffY)) {
                // 좌우 스와이프
                if (Math.abs(diffX) > threshold) {
                    this.changeDirection(diffX > 0 ? 'right' : 'left');
                }
            } else {
                // 상하 스와이프
                if (Math.abs(diffY) > threshold) {
                    this.changeDirection(diffY > 0 ? 'down' : 'up');
                }
            }

            startX = startY = null;
            e.preventDefault();
        });
    }

    changeDirection(newDirection) {
        const directions = {
            'up': { x: 0, y: -1 },
            'down': { x: 0, y: 1 },
            'left': { x: -1, y: 0 },
            'right': { x: 1, y: 0 }
        };

        const newDir = directions[newDirection];
        if (!newDir) return;

        // 반대 방향으로는 이동 불가
        if (this.direction.x === -newDir.x && this.direction.y === -newDir.y) return;

        this.direction = newDir;
    }

    startGame() {
        if (this.isRunning) return;

        this.isRunning = true;

        // 모든 버튼 숨기기
        document.getElementById('snake-start-btn').classList.add('hidden');
        document.getElementById('snake-reset-btn').classList.add('hidden');
        document.getElementById('snake-game-over').classList.add('hidden');

        this.gameLoop = setInterval(() => this.update(), this.speed);
    }

    update() {
        // 뱀 머리 위치 계산
        const head = {
            x: this.snake[0].x + this.direction.x,
            y: this.snake[0].y + this.direction.y
        };

        // 벽 충돌 검사
        if (head.x < 0 || head.x >= this.gridSize || head.y < 0 || head.y >= this.gridSize) {
            this.gameOver();
            return;
        }

        // 자기 몸 충돌 검사
        if (this.snake.some(segment => segment.x === head.x && segment.y === head.y)) {
            this.gameOver();
            return;
        }

        this.snake.unshift(head);

        // 음식 먹기
        if (head.x === this.food.x && head.y === this.food.y) {
            this.score += 10;
            this.updateScore();
            this.generateFood();

            // 속도 증가 (선택사항)
            if (this.speed > 80) {
                this.speed -= 2;
                clearInterval(this.gameLoop);
                this.gameLoop = setInterval(() => this.update(), this.speed);
            }
        } else {
            // 꼬리 제거
            this.snake.pop();
        }

        this.draw();
    }

    draw() {
        // 배경 클리어
        this.ctx.fillStyle = '#1a1a2e';
        this.ctx.fillRect(0, 0, this.canvasSize, this.canvasSize);

        // 격자 그리기 (선택사항)
        this.ctx.strokeStyle = 'rgba(255, 255, 255, 0.1)';
        this.ctx.lineWidth = 1;
        for (let i = 0; i <= this.gridSize; i++) {
            this.ctx.beginPath();
            this.ctx.moveTo(i * this.cellSize, 0);
            this.ctx.lineTo(i * this.cellSize, this.canvasSize);
            this.ctx.stroke();

            this.ctx.beginPath();
            this.ctx.moveTo(0, i * this.cellSize);
            this.ctx.lineTo(this.canvasSize, i * this.cellSize);
            this.ctx.stroke();
        }

        // 뱀 그리기
        this.snake.forEach((segment, index) => {
            const x = segment.x * this.cellSize;
            const y = segment.y * this.cellSize;

            if (index === 0) {
                // 머리 - 더 크고 특별하게
                this.ctx.fillStyle = '#4ecdc4';
                this.ctx.fillRect(x + 1, y + 1, this.cellSize - 2, this.cellSize - 2);

                // 눈
                this.ctx.fillStyle = '#fff';
                this.ctx.fillRect(x + 4, y + 4, 3, 3);
                this.ctx.fillRect(x + this.cellSize - 7, y + 4, 3, 3);

                // 눈동자
                this.ctx.fillStyle = '#000';
                this.ctx.fillRect(x + 5, y + 5, 1, 1);
                this.ctx.fillRect(x + this.cellSize - 6, y + 5, 1, 1);
            } else {
                // 몸통 - 그라데이션 효과
                const opacity = Math.max(0.6, 1 - (index * 0.03));
                this.ctx.fillStyle = `rgba(69, 183, 209, ${opacity})`;
                this.ctx.fillRect(x + 2, y + 2, this.cellSize - 4, this.cellSize - 4);
            }
        });

        // 음식 그리기 - 사과 모양
        const fx = this.food.x * this.cellSize;
        const fy = this.food.y * this.cellSize;

        // 사과 본체
        this.ctx.fillStyle = '#ff6b6b';
        this.ctx.fillRect(fx + 2, fy + 2, this.cellSize - 4, this.cellSize - 4);

        // 사과 줄기
        this.ctx.fillStyle = '#27ae60';
        this.ctx.fillRect(fx + this.cellSize/2 - 1, fy + 1, 2, 4);
    }

    generateFood() {
        do {
            this.food = {
                x: Math.floor(Math.random() * this.gridSize),
                y: Math.floor(Math.random() * this.gridSize)
            };
        } while (this.snake.some(segment => segment.x === this.food.x && segment.y === this.food.y));
    }

    updateScore() {
        document.getElementById('snake-score').textContent = `점수: ${this.score}`;
    }

    resetGame() {
        this.isRunning = false;
        clearInterval(this.gameLoop);
        this.gameLoop = null;

        // 뱀 초기화
        this.snake = [
            { x: Math.floor(this.gridSize / 2), y: Math.floor(this.gridSize / 2) }
        ];
        this.direction = { x: 1, y: 0 };
        this.score = 0;
        this.speed = 150;

        // UI 초기화 - 초기 상태로 되돌리기 (시작 버튼만 보이기)
        document.getElementById('snake-start-btn').classList.remove('hidden');
        document.getElementById('snake-reset-btn').classList.add('hidden');
        document.getElementById('snake-game-over').classList.add('hidden');

        this.updateScore();
        this.generateFood();
        this.draw();
    }

    gameOver() {
        this.isRunning = false;
        clearInterval(this.gameLoop);
        this.gameLoop = null;

        // 최고점수 업데이트
        const highScore = this.getHighScore();
        if (this.score > highScore) {
            this.setHighScore(this.score);
            document.getElementById('snake-high-score').textContent = `최고점수: ${this.score}`;
        }

        // 등급 분석
        const analysis = this.analyzeScore(this.score);

        // 게임오버 화면 표시
        document.getElementById('game-over-score').textContent = `점수: ${this.score}점`;
        document.getElementById('game-over-grade').textContent = analysis.grade;
        document.getElementById('game-over-grade').className = `game-over-grade grade-${analysis.grade.toLowerCase().replace('+', '-plus')}`;
        document.getElementById('game-over-message').textContent = analysis.message;
        document.getElementById('snake-game-over').classList.remove('hidden');

        // 리셋 버튼만 표시
        document.getElementById('snake-start-btn').classList.add('hidden');
        document.getElementById('snake-reset-btn').classList.remove('hidden');
    }

    analyzeScore(score) {
        if (score >= 200) {
            return { grade: 'S+', message: '🐍 뱀게임 마스터! 완벽한 실력이네요! 👑' };
        } else if (score >= 150) {
            return { grade: 'S', message: '🏆 놀라운 실력! 프로 수준이에요!' };
        } else if (score >= 100) {
            return { grade: 'A', message: '⭐ 대단해요! 상당한 실력자네요!' };
        } else if (score >= 60) {
            return { grade: 'B', message: '👍 좋은 실력이에요! 조금만 더 연습하면 고수!' };
        } else if (score >= 30) {
            return { grade: 'C', message: '📈 나쁘지 않아요! 꾸준히 연습해보세요!' };
        } else {
            return { grade: 'D', message: '💪 처음엔 이 정도! 연습하면 금방 늘어요!' };
        }
    }

    getHighScore() {
        try {
            return parseInt(localStorage.getItem('snakeHighScore') || '0');
        } catch (e) {
            return 0;
        }
    }

    setHighScore(score) {
        try {
            localStorage.setItem('snakeHighScore', score.toString());
        } catch (e) {
            console.log('localStorage not available');
        }
    }

    destroy() {
        this.isRunning = false;
        clearInterval(this.gameLoop);

        // 키보드 이벤트 리스너 제거
        if (this.keyHandler) {
            document.removeEventListener('keydown', this.keyHandler);
        }

        // 다른 이벤트 리스너들 제거
        const startBtn = document.getElementById('snake-start-btn');
        const resetBtn = document.getElementById('snake-reset-btn');

        if (startBtn) startBtn.replaceWith(startBtn.cloneNode(true));
        if (resetBtn) resetBtn.replaceWith(resetBtn.cloneNode(true));
    }
}

// 전역에서 사용할 수 있도록 export
window.SnakeGame = SnakeGame;