// static/js/games/reactionTest.js
class ReactionTestGame {
    constructor() {
        this.isRunning = false;
        this.startTime = 0;
        this.gameArea = null;
        this.display = null;
        this.result = null;
        this.waitTimeout = null;
    }

    init(gameArea) {
        this.gameArea = gameArea;
        this.setupUI();
        return this;
    }

    setupUI() {
        this.gameArea.innerHTML = `
            <div class="reaction-game-container">
                <div id="reaction-display" class="reaction-display waiting">
                    <div class="reaction-text">
                        준비하세요... 빨간 화면이 초록색으로 바뀌면 클릭!
                    </div>
                </div>
                <div id="reaction-result" class="reaction-result"></div>
                <div class="reaction-controls">
                    <button id="reaction-start-btn" class="game-btn primary">시작</button>
                    <button id="reaction-reset-btn" class="game-btn secondary">다시</button>
                </div>
            </div>
        `;

        // CSS 스타일 추가
        this.addStyles();

        // 이벤트 리스너 설정
        this.setupEventListeners();
    }

    addStyles() {
        const style = document.createElement('style');
        style.textContent = `
            .reaction-game-container {
                display: flex;
                flex-direction: column;
                align-items: center;
                gap: 20px;
            }

            .reaction-display {
                width: 100%;
                height: 180px;
                border-radius: 15px;
                display: flex;
                align-items: center;
                justify-content: center;
                cursor: pointer;
                transition: all 0.3s ease;
                position: relative;
                overflow: hidden;
            }

            .reaction-display.waiting {
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            }

            .reaction-display.ready {
                background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
                animation: pulse 1s ease-in-out infinite;
            }

            .reaction-display.go {
                background: linear-gradient(135deg, #00d2d3 0%, #54a0ff 100%);
                box-shadow: 0 0 30px rgba(0, 210, 211, 0.5);
            }

            .reaction-display.result {
                background: linear-gradient(135deg, #feca57 0%, #ff9ff3 100%);
            }

            @keyframes pulse {
                0%, 100% { transform: scale(1); }
                50% { transform: scale(1.02); }
            }

            .reaction-text {
                color: white;
                font-size: 1.3rem;
                font-weight: bold;
                text-align: center;
                text-shadow: 0 2px 4px rgba(0,0,0,0.3);
                z-index: 2;
            }

            .reaction-result {
                min-height: 60px;
                color: #fff;
                text-align: center;
                font-size: 1.1rem;
                line-height: 1.5;
            }

            .reaction-controls {
                display: flex;
                gap: 15px;
            }

            .game-btn {
                padding: 12px 24px;
                border: none;
                border-radius: 25px;
                font-size: 1rem;
                font-weight: 500;
                cursor: pointer;
                transition: all 0.3s ease;
                min-width: 100px;
            }

            .game-btn.primary {
                background: linear-gradient(45deg, #7461ee, #ff6b9d);
                color: white;
            }

            .game-btn.primary:hover {
                transform: translateY(-2px);
                box-shadow: 0 8px 20px rgba(116, 97, 238, 0.4);
            }

            .game-btn.secondary {
                background: rgba(255, 255, 255, 0.1);
                color: white;
                border: 1px solid rgba(255, 255, 255, 0.2);
            }

            .game-btn.secondary:hover {
                background: rgba(255, 255, 255, 0.2);
                transform: translateY(-2px);
            }

            .game-btn:disabled {
                opacity: 0.6;
                cursor: not-allowed;
                transform: none !important;
            }

            .grade-display {
                display: inline-block;
                padding: 8px 16px;
                border-radius: 20px;
                font-weight: bold;
                margin: 10px 0;
            }

            .grade-s-plus { background: linear-gradient(45deg, #ffd700, #ffed4e); color: #333; }
            .grade-s { background: linear-gradient(45deg, #c0c0c0, #e8e8e8); color: #333; }
            .grade-a { background: linear-gradient(45deg, #cd7f32, #daa520); color: white; }
            .grade-b { background: linear-gradient(45deg, #4ecdc4, #44a08d); color: white; }
            .grade-c { background: linear-gradient(45deg, #95a5a6, #7f8c8d); color: white; }
        `;

        if (!document.getElementById('reaction-test-styles')) {
            style.id = 'reaction-test-styles';
            document.head.appendChild(style);
        }
    }

    setupEventListeners() {
        const startBtn = document.getElementById('reaction-start-btn');
        const resetBtn = document.getElementById('reaction-reset-btn');
        const display = document.getElementById('reaction-display');

        startBtn.addEventListener('click', () => this.startGame());
        resetBtn.addEventListener('click', () => this.resetGame());
        display.addEventListener('click', () => this.handleDisplayClick());
    }

    startGame() {
        if (this.isRunning) return;

        this.isRunning = true;
        this.display = document.getElementById('reaction-display');
        this.result = document.getElementById('reaction-result');

        const startBtn = document.getElementById('reaction-start-btn');
        startBtn.disabled = true;
        startBtn.textContent = '대기중...';

        // 준비 단계
        this.display.className = 'reaction-display ready';
        this.display.querySelector('.reaction-text').textContent = '잠시 기다리세요...';
        this.result.textContent = '';

        // 랜덤 시간 후 시작 (2-6초)
        const waitTime = Math.random() * 4000 + 2000;

        this.waitTimeout = setTimeout(() => {
            if (!this.isRunning) return;

            this.display.className = 'reaction-display go';
            this.display.querySelector('.reaction-text').textContent = '지금 클릭하세요!';
            this.startTime = performance.now();

        }, waitTime);
    }

    handleDisplayClick() {
        if (!this.isRunning) return;

        const currentTime = performance.now();

        // 아직 초록색이 되기 전에 클릭한 경우
        if (this.startTime === 0) {
            this.endGame('early', 0);
            return;
        }

        // 정상적인 반응
        const reactionTime = currentTime - this.startTime;
        this.endGame('success', reactionTime);
    }

    endGame(type, reactionTime) {
        this.isRunning = false;
        clearTimeout(this.waitTimeout);

        const startBtn = document.getElementById('reaction-start-btn');
        startBtn.disabled = false;
        startBtn.textContent = '다시 시작';

        this.display.className = 'reaction-display result';

        if (type === 'early') {
            this.display.querySelector('.reaction-text').textContent = '너무 빨라요! 😅';
            this.result.innerHTML = `
                <div style="color: #ff6b6b;">
                    아직 신호가 나오지 않았어요!<br>
                    초록색으로 바뀔 때까지 기다려보세요.
                </div>
            `;
            return;
        }

        // 성공한 경우 결과 분석
        const analysis = this.analyzeReactionTime(reactionTime);

        this.display.querySelector('.reaction-text').textContent = `${Math.round(reactionTime)}ms`;
        this.result.innerHTML = `
            <div class="grade-display grade-${analysis.grade.toLowerCase().replace('+', '-plus')}">
                ${analysis.grade} 등급
            </div>
            <div style="margin-top: 10px;">
                ${analysis.message}
            </div>
            <div style="margin-top: 8px; color: #ccc; font-size: 0.9rem;">
                ${analysis.description}
            </div>
        `;

        // 결과에 따른 효과음 (선택사항)
        this.playResultSound(analysis.grade);
    }

    analyzeReactionTime(time) {
        if (time < 150) {
            return {
                grade: 'S+',
                message: '초인간적 반응속도! 🤖',
                description: '프로게이머도 부러워할 반응속도입니다!'
            };
        } else if (time < 200) {
            return {
                grade: 'S',
                message: '프로게이머 수준! 🏆',
                description: '전문 게이머 수준의 놀라운 반응속도네요!'
            };
        } else if (time < 250) {
            return {
                grade: 'A',
                message: '매우 빠름! ⚡',
                description: '상위 5%에 해당하는 빠른 반응속도입니다!'
            };
        } else if (time < 300) {
            return {
                grade: 'B',
                message: '좋은 반응속도! 👍',
                description: '평균보다 빠른 좋은 반응속도예요!'
            };
        } else if (time < 400) {
            return {
                grade: 'C',
                message: '평균 수준! 📈',
                description: '꾸준히 연습하면 더 빨라질 거예요!'
            };
        } else {
            return {
                grade: 'D',
                message: '연습이 필요해요! 💪',
                description: '조금 더 집중해서 연습해보세요!'
            };
        }
    }

    playResultSound(grade) {
        // Web Audio API를 사용한 간단한 효과음
        try {
            const audioContext = new (window.AudioContext || window.webkitAudioContext)();
            const oscillator = audioContext.createOscillator();
            const gainNode = audioContext.createGain();

            oscillator.connect(gainNode);
            gainNode.connect(audioContext.destination);

            // 등급에 따른 다른 음
            let frequency;
            switch(grade) {
                case 'S+':
                case 'S':
                    frequency = 800; // 높은 음
                    break;
                case 'A':
                    frequency = 600;
                    break;
                case 'B':
                    frequency = 500;
                    break;
                default:
                    frequency = 400; // 낮은 음
            }

            oscillator.frequency.setValueAtTime(frequency, audioContext.currentTime);
            oscillator.type = 'sine';

            gainNode.gain.setValueAtTime(0.1, audioContext.currentTime);
            gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.3);

            oscillator.start(audioContext.currentTime);
            oscillator.stop(audioContext.currentTime + 0.3);
        } catch (e) {
            // 오디오 재생 실패 시 무시
            console.log('Audio playback not supported');
        }
    }

    resetGame() {
        this.isRunning = false;
        this.startTime = 0;
        clearTimeout(this.waitTimeout);

        const startBtn = document.getElementById('reaction-start-btn');
        const display = document.getElementById('reaction-display');
        const result = document.getElementById('reaction-result');

        startBtn.disabled = false;
        startBtn.textContent = '시작';

        display.className = 'reaction-display waiting';
        display.querySelector('.reaction-text').textContent = '준비하세요... 빨간 화면이 초록색으로 바뀌면 클릭!';
        result.textContent = '';
    }

    destroy() {
        this.isRunning = false;
        clearTimeout(this.waitTimeout);

        // 이벤트 리스너 제거
        const startBtn = document.getElementById('reaction-start-btn');
        const resetBtn = document.getElementById('reaction-reset-btn');
        const display = document.getElementById('reaction-display');

        if (startBtn) startBtn.replaceWith(startBtn.cloneNode(true));
        if (resetBtn) resetBtn.replaceWith(resetBtn.cloneNode(true));
        if (display) display.replaceWith(display.cloneNode(true));
    }
}

// 전역에서 사용할 수 있도록 export
window.ReactionTestGame = ReactionTestGame;