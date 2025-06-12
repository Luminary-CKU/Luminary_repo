/**
 * 🌌 Cards Galaxy - AI 기반 챔피언 추천 시스템
 * 최신 트렌디한 3D 인터랙티브 디자인 + 실시간 데이터 분석
 */

class CardsGalaxySystem {
    constructor(championRecommendations, playerData, currentVersion) {
        this.championRecommendations = championRecommendations;
        this.playerData = playerData || this.getDefaultPlayerData();
        this.currentVersion = currentVersion || '14.23.1';
        this.selectedFilters = {
            role: 'ALL',
            difficulty: 'ALL',
            tier: 'ALL'
        };
        this.champions = [];
        this.filteredChampions = [];
        this.animationFrameId = null;
        
        console.log('🌌 Cards Galaxy 시스템 초기화', {
            recommendations: this.championRecommendations,
            player: this.playerData,
            version: this.currentVersion
        });
        
        this.init();
    }

    async init() {
        try {
            console.log('🚀 Cards Galaxy 초기화 시작');
            
            // 1. UI 컴포넌트 초기화
            this.initializeUI();
            
            // 2. 데이터 로드
            await this.loadChampionData();
            
            // 3. 이벤트 리스너 설정
            this.setupEventListeners();
            
            // 4. 애니메이션 시작
            this.startAnimations();
            
            // 5. 로딩 완료 후 표시
            setTimeout(() => {
                this.hideLoading();
                this.renderChampions();
            }, 2000);
            
            console.log('✅ Cards Galaxy 초기화 완료');
            
        } catch (error) {
            console.error('❌ Cards Galaxy 초기화 실패:', error);
            this.handleInitializationError();
        }
    }

    /**
     * 🌟 별들 애니메이션
     */
    animateStars() {
        const starsContainer = document.querySelector('.stars');
        if (!starsContainer) return;

        // 100개의 별 생성
        for (let i = 0; i < 100; i++) {
            const star = document.createElement('div');
            star.className = 'star';
            star.style.left = Math.random() * 100 + '%';
            star.style.top = Math.random() * 100 + '%';
            star.style.animationDelay = Math.random() * 3 + 's';
            star.style.animationDuration = (Math.random() * 2 + 1) + 's';
            starsContainer.appendChild(star);
        }
    }

    /**
     * 🌀 궤도 링 애니메이션
     */
    animateOrbitRings() {
        const rings = document.querySelectorAll('.orbit-ring');
        rings.forEach((ring, index) => {
            const duration = 20 + (index * 10);
            ring.style.animation = `rotate ${duration}s linear infinite`;
        });
    }

    /**
     * 💫 플레이어 허브 펄스 애니메이션
     */
    animatePlayerHub() {
        const hub = document.getElementById('playerHub');
        if (hub) {
            hub.style.animation = 'hubPulse 3s ease-in-out infinite';
        }
    }

    /**
     * ✨ 우주 파티클 애니메이션
     */
    animateCosmicParticles() {
        const particlesContainer = document.querySelector('.cosmic-particles');
        if (!particlesContainer) return;

        for (let i = 0; i < 20; i++) {
            const particle = document.createElement('div');
            particle.className = 'cosmic-particle';
            particle.style.left = Math.random() * 100 + '%';
            particle.style.top = Math.random() * 100 + '%';
            particle.style.animationDelay = Math.random() * 5 + 's';
            particle.style.animationDuration = (Math.random() * 4 + 3) + 's';
            particlesContainer.appendChild(particle);
        }
    }

    /**
     * 🎴 챔피언 카드 렌더링
     */
    renderChampions() {
        const container = document.getElementById('championCards');
        if (!container) return;

        container.innerHTML = '';

        this.filteredChampions.forEach((champion, index) => {
            const card = this.createChampionCard(champion, index);
            container.appendChild(card);
            
            // 스타일링된 등장 애니메이션
            setTimeout(() => {
                card.classList.add('card-visible');
            }, index * 100);
        });

        // 통계 업데이트
        this.updateStats();
    }

    /**
     * 🃏 개별 챔피언 카드 생성
     */
    createChampionCard(champion, index) {
        const card = document.createElement('div');
        card.className = `champion-card ${this.getCardTier(champion.confidence)}`;
        card.dataset.champion = champion.name;
        card.dataset.role = champion.role;
        card.dataset.difficulty = champion.difficulty;
        card.dataset.tier = champion.tier;

        // 궤도별 위치 계산
        const tier = this.getCardTier(champion.confidence);
        const radius = tier === 'card-tier-1' ? 250 : tier === 'card-tier-2' ? 350 : 450;
        const angle = (360 / this.filteredChampions.length) * index;

        card.style.setProperty('--angle', `${angle}deg`);
        card.style.setProperty('--radius', `${radius}px`);
        card.style.setProperty('--delay', `${index * 0.1}s`);

        // 카드 내용
        card.innerHTML = `
            <div class="card-glow"></div>
            <div class="champion-avatar-large">
                <img src="https://ddragon.leagueoflegends.com/cdn/${this.currentVersion}/img/champion/${champion.name}.png" 
                     alt="${champion.name}" 
                     onerror="this.src='data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iODAiIGhlaWdodD0iODAiIHZpZXdCb3g9IjAgMCA4MCA4MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHJlY3Qgd2lkdGg9IjgwIiBoZWlnaHQ9IjgwIiBmaWxsPSIjNjY3ZWVhIiByeD0iNDAiLz4KPHRleHQgeD0iNDAiIHk9IjQ1IiBmb250LWZhbWlseT0ic2Fucy1zZXJpZiIgZm9udC1zaXplPSIyNCIgZmlsbD0id2hpdGUiIHRleHQtYW5jaG9yPSJtaWRkbGUiPj88L3RleHQ+Cjwvc3ZnPg=='">
                <div class="confidence-badge">${champion.confidence}%</div>
            </div>
            
            <div class="champion-name">${champion.name}</div>
            
            <div class="champion-role-badge role-${champion.role.toLowerCase()}">${champion.role}</div>
            
            <div class="champion-meta-info">
                <span class="meta-tier tier-${champion.tier.toLowerCase()}">${champion.tier}-Tier</span>
                <span class="difficulty diff-${champion.difficulty}">${champion.difficulty}</span>
            </div>
            
            <div class="champion-tags">
                ${champion.tags.map(tag => `<span class="tag tag-${tag}">${tag}</span>`).join('')}
            </div>
            
            <div class="match-scores">
                <div class="match-score">
                    <span class="score-label">Style</span>
                    <div class="score-bar">
                        <div class="score-fill" style="width: ${champion.styleMatch}%"></div>
                    </div>
                    <span class="score-value">${champion.styleMatch}%</span>
                </div>
                <div class="match-score">
                    <span class="score-label">Skill</span>
                    <div class="score-bar">
                        <div class="score-fill" style="width: ${champion.skillMatch}%"></div>
                    </div>
                    <span class="score-value">${champion.skillMatch}%</span>
                </div>
            </div>
            
            <div class="card-type-indicator type-${champion.type?.toLowerCase()?.replace('_', '-') || 'recommended'}">
                ${this.getTypeIcon(champion.type)} ${this.getTypeLabel(champion.type)}
            </div>
        `;

        // 이벤트 리스너 추가
        card.addEventListener('click', () => this.showDetailPanel(champion));
        card.addEventListener('mouseenter', () => this.highlightCard(card));
        card.addEventListener('mouseleave', () => this.unhighlightCard(card));

        return card;
    }

    /**
     * 🎯 상세 정보 패널 표시
     */
    showDetailPanel(champion) {
        const panel = document.getElementById('detailPanel');
        const nameEl = document.getElementById('detailChampionName');
        const confidenceEl = document.getElementById('detailConfidence');
        const roleEl = document.getElementById('detailRole');
        const avatarEl = document.getElementById('detailAvatar');
        const reasonsEl = document.getElementById('recommendationReasons');
        const statsEl = document.getElementById('personalStats');

        if (!panel) return;

        // 기본 정보 설정
        nameEl.textContent = champion.name;
        confidenceEl.textContent = `${champion.confidence}% Match`;
        roleEl.textContent = champion.role;
        
        // 아바타 설정
        avatarEl.innerHTML = `
            <img src="https://ddragon.leagueoflegends.com/cdn/${this.currentVersion}/img/champion/${champion.name}.png" 
                 alt="${champion.name}"
                 onerror="this.style.display='none'">
        `;

        // 추천 이유들 설정
        reasonsEl.innerHTML = champion.reasons.map(reason => `
            <div class="reason-item">
                <div class="reason-icon">✓</div>
                <span>${reason}</span>
            </div>
        `).join('');

        // 개인 통계 (있는 경우)
        if (champion.personalStats) {
            statsEl.style.display = 'block';
            statsEl.innerHTML = `
                <h4>Your Performance</h4>
                <div class="personal-stat">
                    <span>Games Played:</span>
                    <span>${champion.personalStats.gamesPlayed}</span>
                </div>
                <div class="personal-stat">
                    <span>Win Rate:</span>
                    <span>${champion.personalStats.winRate}%</span>
                </div>
                <div class="personal-stat">
                    <span>Average KDA:</span>
                    <span>${champion.personalStats.averageKDA}</span>
                </div>
            `;
        } else {
            statsEl.style.display = 'none';
        }

        // 패널 표시
        panel.classList.add('show');
        
        // 선택된 챔피언 하이라이트
        this.highlightSelectedChampion(champion.name);
    }

    /**
     * 📱 상세 패널 숨기기
     */
    hideDetailPanel() {
        const panel = document.getElementById('detailPanel');
        if (panel) {
            panel.classList.remove('show');
        }
        this.clearChampionHighlight();
    }

    /**
     * 🔍 필터 적용
     */
    applyFilters() {
        this.filteredChampions = this.champions.filter(champion => {
            const roleMatch = this.selectedFilters.role === 'ALL' || 
                             champion.role === this.selectedFilters.role;
            
            const difficultyMatch = this.selectedFilters.difficulty === 'ALL' || 
                                   champion.difficulty === this.selectedFilters.difficulty;
            
            const tierMatch = this.selectedFilters.tier === 'ALL' || 
                             champion.tier === this.selectedFilters.tier;

            return roleMatch && difficultyMatch && tierMatch;
        });

        console.log(`🔍 필터 적용: ${this.filteredChampions.length}/${this.champions.length} 챔피언`);
        
        this.renderChampions();
        this.showFilterMessage();
    }

    /**
     * 🔄 필터 리셋
     */
    resetFilters() {
        this.selectedFilters = {
            role: 'ALL',
            difficulty: 'ALL',
            tier: 'ALL'
        };

        // 모든 필터 버튼 리셋
        document.querySelectorAll('.filter-btn').forEach(btn => {
            btn.classList.remove('active');
            if (btn.textContent === 'All') {
                btn.classList.add('active');
            }
        });

        this.applyFilters();
    }

    /**
     * 📊 통계 업데이트
     */
    updateStats() {
        const totalEl = document.getElementById('totalChampions');
        const perfectEl = document.getElementById('perfectMatches');
        const challengesEl = document.getElementById('newChallenges');

        if (totalEl) totalEl.textContent = this.filteredChampions.length;
        
        if (perfectEl) {
            const perfectMatches = this.filteredChampions.filter(c => c.confidence >= 90).length;
            perfectEl.textContent = perfectMatches;
        }
        
        if (challengesEl) {
            const challenges = this.filteredChampions.filter(c => c.type === 'NEW_CHALLENGE').length;
            challengesEl.textContent = challenges;
        }
    }

    /**
     * 🖱️ 마우스 이동 이벤트 처리
     */
    handleMouseMove(e) {
        const cards = document.querySelectorAll('.champion-card');
        const centerX = window.innerWidth / 2;
        const centerY = window.innerHeight / 2;

        const deltaX = (e.clientX - centerX) / centerX;
        const deltaY = (e.clientY - centerY) / centerY;

        cards.forEach((card, index) => {
            const intensity = 0.3 + (index * 0.02);
            const rotateX = deltaY * intensity * 3;
            const rotateY = deltaX * intensity * 3;

            card.style.transform = `
                perspective(1000px) 
                rotateX(${rotateX}deg) 
                rotateY(${rotateY}deg) 
                translateZ(10px)
            `;
        });

        // 플레이어 허브도 약간 반응
        const hub = document.getElementById('playerHub');
        if (hub) {
            hub.style.transform = `
                translate(-50%, -50%) 
                rotateX(${deltaY * 5}deg) 
                rotateY(${deltaX * 5}deg)
            `;
        }
    }

    /**
     * 🎨 카드 하이라이트
     */
    highlightCard(card) {
        // 다른 카드들 dim 처리
        document.querySelectorAll('.champion-card').forEach(c => {
            if (c !== card) {
                c.style.opacity = '0.4';
                c.style.transform += ' scale(0.95)';
            }
        });

        // 선택된 카드 강조
        card.style.opacity = '1';
        card.style.transform += ' scale(1.05) translateZ(50px)';
        card.classList.add('highlighted');
    }

    /**
     * 🎨 카드 하이라이트 해제
     */
    unhighlightCard(card) {
        // 모든 카드 원래대로
        document.querySelectorAll('.champion-card').forEach(c => {
            c.style.opacity = '1';
            c.classList.remove('highlighted');
        });
    }

    /**
     * 🌟 선택된 챔피언 하이라이트
     */
    highlightSelectedChampion(championName) {
        document.querySelectorAll('.champion-card').forEach(card => {
            if (card.dataset.champion === championName) {
                card.classList.add('selected');
            } else {
                card.classList.remove('selected');
                card.style.opacity = '0.3';
            }
        });
    }

    /**
     * 🌟 챔피언 하이라이트 제거
     */
    clearChampionHighlight() {
        document.querySelectorAll('.champion-card').forEach(card => {
            card.classList.remove('selected');
            card.style.opacity = '1';
        });
    }

    /**
     * 📱 로딩 화면 숨기기
     */
    hideLoading() {
        const loadingOverlay = document.getElementById('loadingOverlay');
        if (loadingOverlay) {
            loadingOverlay.style.opacity = '0';
            setTimeout(() => {
                loadingOverlay.style.display = 'none';
            }, 500);
        }
    }

    /**
     * 💬 필터 메시지 표시
     */
    showFilterMessage() {
        if (this.filteredChampions.length === 0) {
            this.showNoResultsMessage();
            return;
        }

        // 필터 결과 토스트 메시지
        this.showToast(`${this.filteredChampions.length}개 챔피언을 찾았습니다!`);
    }

    /**
     * 🚫 결과 없음 메시지
     */
    showNoResultsMessage() {
        const container = document.getElementById('championCards');
        if (container) {
            container.innerHTML = `
                <div class="no-results">
                    <div class="no-results-icon">🔍</div>
                    <div class="no-results-title">검색 결과가 없습니다</div>
                    <div class="no-results-subtitle">필터 조건을 조정해보세요</div>
                    <button class="no-results-btn" onclick="galaxySystem.resetFilters()">
                        필터 초기화
                    </button>
                </div>
            `;
        }
    }

    /**
     * 🍞 토스트 메시지 표시
     */
    showToast(message, duration = 3000) {
        // 기존 토스트 제거
        const existingToast = document.querySelector('.toast-message');
        if (existingToast) {
            existingToast.remove();
        }

        const toast = document.createElement('div');
        toast.className = 'toast-message';
        toast.textContent = message;
        document.body.appendChild(toast);

        // 애니메이션
        setTimeout(() => toast.classList.add('show'), 100);
        setTimeout(() => {
            toast.classList.remove('show');
            setTimeout(() => toast.remove(), 300);
        }, duration);
    }

    // ===== 유틸리티 메서드들 =====

    getCardTier(confidence) {
        if (confidence >= 85) return 'card-tier-1';
        if (confidence >= 70) return 'card-tier-2';
        return 'card-tier-3';
    }

    getMetaTier(metaStrength) {
        if (metaStrength >= 8.5) return 'S';
        if (metaStrength >= 7.0) return 'A';
        if (metaStrength >= 5.5) return 'B';
        if (metaStrength >= 4.0) return 'C';
        return 'D';
    }

    generateTags(rec) {
        const tags = [];
        if (rec.personalStats) tags.push('personal');
        if (rec.metaStrength >= 8.0) tags.push('meta');
        if (rec.styleMatch >= 0.8) tags.push('style');
        if (rec.recommendationType === 'NEW_CHALLENGE') tags.push('challenge');
        return tags;
    }

    getTypeIcon(type) {
        const icons = {
            'PROVEN': '✅',
            'PERFECT_MATCH': '🎯',
            'NEW_CHALLENGE': '🚀',
            'RECOMMENDED': '⭐'
        };
        return icons[type] || '⭐';
    }

    getTypeLabel(type) {
        const labels = {
            'PROVEN': 'Proven',
            'PERFECT_MATCH': 'Perfect',
            'NEW_CHALLENGE': 'Challenge',
            'RECOMMENDED': 'Recommended'
        };
        return labels[type] || 'Recommended';
    }

    getFilterType(button) {
        if (button.hasAttribute('data-role')) return 'role';
        if (button.hasAttribute('data-difficulty')) return 'difficulty';
        if (button.hasAttribute('data-tier')) return 'tier';
        return 'role';
    }

    getDefaultPlayerData() {
        return {
            name: 'Player',
            winRate: 67,
            averageKDA: 2.4,
            tier: 'GOLD'
        };
    }

    getDefaultChampions() {
        return [
            {
                name: 'Jinx',
                role: 'ADC',
                confidence: 92,
                difficulty: '보통',
                tier: 'S',
                tags: ['meta', 'personal'],
                reasons: ['높은 개인 승률', '현재 메타 강세', '공격적 플레이스타일 매칭'],
                styleMatch: 88,
                skillMatch: 75,
                type: 'PROVEN',
                metaStrength: 8.5
            },
            {
                name: 'Yasuo',
                role: 'MID',
                confidence: 85,
                difficulty: '어려움',
                tier: 'A',
                tags: ['style', 'challenge'],
                reasons: ['높은 숙련도 상한', '공격적 플레이스타일', '캐리 잠재력'],
                styleMatch: 92,
                skillMatch: 65,
                type: 'NEW_CHALLENGE',
                metaStrength: 7.2
            }
            // ... 더 많은 기본 챔피언들
        ];
    }

    handleInitializationError() {
        const container = document.getElementById('galaxyContainer');
        if (container) {
            container.innerHTML = `
                <div class="error-state">
                    <div class="error-icon">⚠️</div>
                    <div class="error-title">시스템 초기화 실패</div>
                    <div class="error-message">잠시 후 다시 시도해주세요</div>
                    <button class="error-retry-btn" onclick="location.reload()">
                        🔄 다시 시도
                    </button>
                </div>
            `;
        }
    }

    // ===== 액션 메서드들 =====

    openChampionGuide() {
        const championName = document.getElementById('detailChampionName')?.textContent;
        if (championName) {
            const url = `https://www.op.gg/champions/${championName.toLowerCase()}/build`;
            window.open(url, '_blank');
        }
    }

    addToFavorites() {
        const championName = document.getElementById('detailChampionName')?.textContent;
        if (championName) {
            this.showToast(`${championName}을(를) 즐겨찾기에 추가했습니다! ⭐`);
        }
    }

    shareTip() {
        const championName = document.getElementById('detailChampionName')?.textContent;
        if (championName && navigator.share) {
            navigator.share({
                title: `${championName} 추천`,
                text: `Cards Galaxy AI가 추천하는 챔피언: ${championName}`,
                url: window.location.href
            });
        } else {
            this.showToast('추천 정보가 클립보드에 복사되었습니다! 📋');
        }
    }

    toggleStatsPanel() {
        const panel = document.getElementById('statsPanel');
        if (panel) {
            panel.classList.toggle('expanded');
        }
    }

    // ===== CSS 스타일 주입 =====

    injectStyles() {
        if (document.getElementById('galaxy-styles')) return;

        const style = document.createElement('style');
        style.id = 'galaxy-styles';
        style.textContent = `
            /* Cards Galaxy 전용 스타일 */
            .champion-cards-container {
                position: absolute;
                top: 50%;
                left: 50%;
                transform: translate(-50%, -50%);
                width: 100%;
                height: 100%;
                pointer-events: none;
            }

            .champion-card {
                position: absolute;
                width: 180px;
                height: 240px;
                background: linear-gradient(145deg, rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0.05));
                border: 1px solid rgba(255, 255, 255, 0.2);
                border-radius: 20px;
                padding: 15px;
                cursor: pointer;
                transition: all 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275);
                transform-style: preserve-3d;
                backdrop-filter: blur(20px);
                box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
                pointer-events: auto;
                opacity: 0;
                transform: translate(-50%, -50%) scale(0.8);
                left: 50%;
                top: 50%;
            }

            .champion-card.card-visible {
                opacity: 1;
                transform: translate(-50%, -50%) scale(1);
            }

            .champion-card.card-tier-1 {
                animation: orbit-tier-1 25s linear infinite;
            }

            .champion-card.card-tier-2 {
                animation: orbit-tier-2 35s linear infinite;
            }

            .champion-card.card-tier-3 {
                animation: orbit-tier-3 45s linear infinite;
            }

            @keyframes orbit-tier-1 {
                from { transform: translate(-50%, -50%) rotate(0deg) translateX(250px) rotate(0deg); }
                to { transform: translate(-50%, -50%) rotate(360deg) translateX(250px) rotate(-360deg); }
            }

            @keyframes orbit-tier-2 {
                from { transform: translate(-50%, -50%) rotate(0deg) translateX(350px) rotate(0deg); }
                to { transform: translate(-50%, -50%) rotate(360deg) translateX(350px) rotate(-360deg); }
            }

            @keyframes orbit-tier-3 {
                from { transform: translate(-50%, -50%) rotate(0deg) translateX(450px) rotate(0deg); }
                to { transform: translate(-50%, -50%) rotate(360deg) translateX(450px) rotate(-360deg); }
            }

            .champion-card:hover {
                background: linear-gradient(145deg, rgba(102, 126, 234, 0.3), rgba(255, 107, 157, 0.2));
                border-color: rgba(102, 126, 234, 0.8);
                box-shadow: 0 25px 80px rgba(102, 126, 234, 0.4);
                transform: translate(-50%, -50%) scale(1.1) translateZ(60px);
            }

            .champion-card.selected {
                background: linear-gradient(145deg, rgba(255, 215, 0, 0.3), rgba(255, 193, 7, 0.2));
                border-color: rgba(255, 215, 0, 0.8);
                box-shadow: 0 0 60px rgba(255, 215, 0, 0.6);
            }

            .card-glow {
                position: absolute;
                top: -2px;
                left: -2px;
                right: -2px;
                bottom: -2px;
                background: linear-gradient(45deg, transparent, rgba(255, 255, 255, 0.1), transparent);
                border-radius: 20px;
                opacity: 0;
                transition: opacity 0.3s ease;
            }

            .champion-card:hover .card-glow {
                opacity: 1;
                animation: hologram 1.5s ease-in-out infinite;
            }

            @keyframes hologram {
                0% { transform: translateX(-100%) skewX(-45deg); }
                100% { transform: translateX(200%) skewX(-45deg); }
            }

            .champion-avatar-large {
                position: relative;
                width: 80px;
                height: 80px;
                margin: 0 auto 10px;
                border-radius: 50%;
                overflow: hidden;
                border: 3px solid rgba(255, 255, 255, 0.3);
            }

            .champion-avatar-large img {
                width: 100%;
                height: 100%;
                object-fit: cover;
            }

            .confidence-badge {
                position: absolute;
                top: -8px;
                right: -8px;
                background: linear-gradient(45deg, #4ecdc4, #44a08d);
                color: white;
                padding: 4px 8px;
                border-radius: 12px;
                font-size: 0.7rem;
                font-weight: bold;
                box-shadow: 0 2px 8px rgba(78, 205, 196, 0.3);
            }

            .champion-name {
                font-size: 1rem;
                font-weight: bold;
                text-align: center;
                margin-bottom: 8px;
                color: white;
            }

            .champion-role-badge {
                display: inline-block;
                padding: 4px 10px;
                border-radius: 12px;
                font-size: 0.7rem;
                font-weight: bold;
                text-align: center;
                margin-bottom: 8px;
            }

            .champion-role-badge.role-top { background: linear-gradient(45deg, #e74c3c, #c0392b); }
            .champion-role-badge.role-jungle { background: linear-gradient(45deg, #27ae60, #229954); }
            .champion-role-badge.role-mid { background: linear-gradient(45deg, #3498db, #2980b9); }
            .champion-role-badge.role-adc { background: linear-gradient(45deg, #f39c12, #e67e22); }
            .champion-role-badge.role-support { background: linear-gradient(45deg, #9b59b6, #8e44ad); }

            .champion-meta-info {
                display: flex;
                justify-content: space-between;
                margin-bottom: 8px;
                font-size: 0.7rem;
            }

            .meta-tier {
                padding: 2px 6px;
                border-radius: 8px;
                font-weight: bold;
            }

            .meta-tier.tier-s { background: linear-gradient(45deg, #ffd700, #ffed4e); color: #333; }
            .meta-tier.tier-a { background: linear-gradient(45deg, #c0c0c0, #e8e8e8); color: #333; }
            .meta-tier.tier-b { background: linear-gradient(45deg, #cd7f32, #daa520); color: white; }
            .meta-tier.tier-c { background: linear-gradient(45deg, #95a5a6, #7f8c8d); color: white; }
            .meta-tier.tier-d { background: linear-gradient(45deg, #e74c3c, #c0392b); color: white; }

            .difficulty {
                padding: 2px 6px;
                border-radius: 8px;
                font-weight: bold;
            }

            .difficulty.diff-쉬움 { background: rgba(46, 204, 113, 0.3); color: #2ecc71; }
            .difficulty.diff-보통 { background: rgba(241, 196, 15, 0.3); color: #f1c40f; }
            .difficulty.diff-어려움 { background: rgba(231, 76, 60, 0.3); color: #e74c3c; }

            .champion-tags {
                display: flex;
                flex-wrap: wrap;
                gap: 4px;
                margin-bottom: 8px;
                justify-content: center;
            }

            .tag {
                padding: 2px 6px;
                border-radius: 8px;
                font-size: 0.6rem;
                font-weight: bold;
            }

            .tag.tag-meta { background: rgba(255, 107, 157, 0.3); color: #ff6b9d; }
            .tag.tag-personal { background: rgba(78, 205, 196, 0.3); color: #4ecdc4; }
            .tag.tag-style { background: rgba(255, 193, 7, 0.3); color: #ffc107; }
            .tag.tag-challenge { background: rgba(156, 39, 176, 0.3); color: #9c27b0; }

            .match-scores {
                margin-bottom: 8px;
            }

            .match-score {
                display: flex;
                align-items: center;
                gap: 8px;
                margin-bottom: 4px;
                font-size: 0.7rem;
            }

            .score-label {
                min-width: 30px;
                color: rgba(255, 255, 255, 0.7);
            }

            .score-bar {
                flex: 1;
                height: 4px;
                background: rgba(255, 255, 255, 0.2);
                border-radius: 2px;
                overflow: hidden;
            }

            .score-fill {
                height: 100%;
                background: linear-gradient(90deg, #4ecdc4, #44a08d);
                border-radius: 2px;
                transition: width 0.3s ease;
            }

            .score-value {
                min-width: 25px;
                color: #4ecdc4;
                font-weight: bold;
            }

            .card-type-indicator {
                position: absolute;
                bottom: 8px;
                left: 8px;
                right: 8px;
                padding: 4px 8px;
                border-radius: 10px;
                font-size: 0.6rem;
                font-weight: bold;
                text-align: center;
            }

            .card-type-indicator.type-proven {
                background: linear-gradient(45deg, #2ecc71, #27ae60);
                color: white;
            }

            .card-type-indicator.type-perfect-match {
                background: linear-gradient(45deg, #e74c3c, #c0392b);
                color: white;
            }

            .card-type-indicator.type-new-challenge {
                background: linear-gradient(45deg, #9b59b6, #8e44ad);
                color: white;
            }

            .card-type-indicator.type-recommended {
                background: linear-gradient(45deg, #3498db, #2980b9);
                color: white;
            }

            /* 플레이어 허브 스타일 */
            .player-hub {
                position: absolute;
                top: 50%;
                left: 50%;
                transform: translate(-50%, -50%);
                width: 200px;
                height: 200px;
                background: conic-gradient(from 0deg, rgba(102, 126, 234, 0.8), rgba(255, 107, 157, 0.8), rgba(78, 205, 196, 0.8), rgba(102, 126, 234, 0.8));
                border-radius: 50%;
                display: flex;
                align-items: center;
                justify-content: center;
                z-index: 5;
            }

            @keyframes rotate {
                from { transform: translate(-50%, -50%) rotate(0deg); }
                to { transform: translate(-50%, -50%) rotate(360deg); }
            }

            @keyframes hubPulse {
                0%, 100% { box-shadow: 0 0 20px rgba(102, 126, 234, 0.4); }
                50% { box-shadow: 0 0 60px rgba(102, 126, 234, 0.8); }
            }

            .player-info {
                width: 180px;
                height: 180px;
                background: rgba(0, 0, 0, 0.9);
                border-radius: 50%;
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                backdrop-filter: blur(20px);
                text-align: center;
                position: relative;
            }

            .player-avatar {
                width: 60px;
                height: 60px;
                border-radius: 50%;
                background: linear-gradient(45deg, #ff6b6b, #4ecdc4);
                margin-bottom: 10px;
                border: 3px solid rgba(255, 255, 255, 0.3);
            }

            .player-name {
                font-size: 1rem;
                font-weight: bold;
                margin-bottom: 5px;
                color: white;
            }

            .player-stats {
                font-size: 0.7rem;
                color: rgba(255, 255, 255, 0.7);
                margin-bottom: 5px;
            }

            .player-tier {
                font-size: 0.8rem;
                color: #ffc107;
                font-weight: bold;
            }

            /* 궤도 링 스타일 */
            .orbit-ring {
                position: absolute;
                border: 1px solid rgba(255, 255, 255, 0.1);
                border-radius: 50%;
                top: 50%;
                left: 50%;
                transform: translate(-50%, -50%);
                pointer-events: none;
            }

            .orbit-ring.ring-1 {
                width: 500px;
                height: 500px;
                border-color: rgba(102, 126, 234, 0.3);
                animation: rotate 30s linear infinite;
            }

            .orbit-ring.ring-2 {
                width: 700px;
                height: 700px;
                border-color: rgba(255, 107, 157, 0.2);
                animation: rotate 40s linear infinite reverse;
            }

            .orbit-ring.ring-3 {
                width: 900px;
                height: 900px;
                border-color: rgba(78, 205, 196, 0.15);
                animation: rotate 50s linear infinite;
            }

            /* 별과 파티클 */
            .stars {
                position: absolute;
                width: 100%;
                height: 100%;
                pointer-events: none;
            }

            .star {
                position: absolute;
                width: 2px;
                height: 2px;
                background: white;
                border-radius: 50%;
                animation: twinkle 2s ease-in-out infinite alternate;
            }

            @keyframes twinkle {
                from { opacity: 0.3; transform: scale(1); }
                to { opacity: 1; transform: scale(1.2); }
            }

            .cosmic-particles {
                position: absolute;
                width: 100%;
                height: 100%;
                pointer-events: none;
            }

            .cosmic-particle {
                position: absolute;
                width: 4px;
                height: 4px;
                background: radial-gradient(circle, rgba(78, 205, 196, 0.8), transparent);
                border-radius: 50%;
                animation: float 6s ease-in-out infinite;
            }

            @keyframes float {
                0%, 100% { transform: translateY(0px) rotate(0deg); opacity: 0.4; }
                25% { transform: translateY(-30px) rotate(90deg); opacity: 1; }
                50% { transform: translateY(-60px) rotate(180deg); opacity: 0.8; }
                75% { transform: translateY(-30px) rotate(270deg); opacity: 1; }
            }

            /* 헤더 스타일 */
            .galaxy-header {
                position: fixed;
                top: 20px;
                left: 50%;
                transform: translateX(-50%);
                text-align: center;
                z-index: 100;
                backdrop-filter: blur(10px);
                background: rgba(0, 0, 0, 0.3);
                padding: 20px;
                border-radius: 20px;
                border: 1px solid rgba(255, 255, 255, 0.1);
            }

            .galaxy-title {
                font-size: 2rem;
                font-weight: 300;
                background: linear-gradient(45deg, #667eea, #764ba2, #ff6b9d);
                -webkit-background-clip: text;
                -webkit-text-fill-color: transparent;
                background-clip: text;
                margin-bottom: 5px;
            }

            .galaxy-subtitle {
                font-size: 0.9rem;
                color: rgba(255, 255, 255, 0.7);
                margin-bottom: 10px;
            }

            .player-info-badge {
                display: flex;
                gap: 15px;
                justify-content: center;
                font-size: 0.8rem;
            }

            .player-info-badge .player-name {
                color: #4ecdc4;
                font-weight: bold;
            }

            .player-info-badge .player-stats {
                color: rgba(255, 255, 255, 0.7);
            }

            /* 상세 패널 */
            .detail-panel {
                position: fixed;
                bottom: 20px;
                left: 20px;
                width: 380px;
                max-height: 600px;
                background: rgba(0, 0, 0, 0.95);
                border: 1px solid rgba(255, 255, 255, 0.2);
                border-radius: 20px;
                padding: 25px;
                backdrop-filter: blur(20px);
                transform: translateY(100%);
                transition: transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
                z-index: 200;
                overflow-y: auto;
            }

            .detail-panel.show {
                transform: translateY(0);
            }

            .detail-header {
                display: flex;
                align-items: center;
                margin-bottom: 20px;
                position: relative;
            }

            .detail-avatar {
                width: 60px;
                height: 60px;
                border-radius: 50%;
                margin-right: 15px;
                border: 2px solid rgba(255, 255, 255, 0.3);
                overflow: hidden;
            }

            .detail-avatar img {
                width: 100%;
                height: 100%;
                object-fit: cover;
            }

            .detail-info {
                flex: 1;
            }

            .detail-info h3 {
                margin-bottom: 5px;
                font-size: 1.3rem;
                color: white;
            }

            .detail-score {
                background: linear-gradient(45deg, #4ecdc4, #44a08d);
                color: white;
                padding: 4px 12px;
                border-radius: 12px;
                font-size: 0.8rem;
                font-weight: bold;
                width: fit-content;
                margin-bottom: 5px;
            }

            .detail-role {
                color: rgba(255, 255, 255, 0.7);
                font-size: 0.9rem;
            }

            .detail-close-btn {
                position: absolute;
                top: -5px;
                right: -5px;
                width: 30px;
                height: 30px;
                border: none;
                background: rgba(255, 255, 255, 0.1);
                color: white;
                border-radius: 50%;
                cursor: pointer;
                font-size: 1.2rem;
                transition: all 0.3s ease;
            }

            .detail-close-btn:hover {
                background: rgba(255, 255, 255, 0.2);
                transform: scale(1.1);
            }

            .recommendation-reasons {
                margin-bottom: 20px;
            }

            .reason-item {
                display: flex;
                align-items: center;
                margin-bottom: 10px;
                font-size: 0.9rem;
                color: rgba(255, 255, 255, 0.8);
            }

            .reason-icon {
                width: 20px;
                height: 20px;
                background: #4ecdc4;
                border-radius: 50%;
                margin-right: 12px;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 0.7rem;
                color: white;
                font-weight: bold;
                flex-shrink: 0;
            }

            .personal-stats {
                background: rgba(255, 255, 255, 0.05);
                padding: 15px;
                border-radius: 12px;
                margin-bottom: 20px;
            }

            .personal-stats h4 {
                color: #ffc107;
                margin-bottom: 10px;
                font-size: 1rem;
            }

            .personal-stat {
                display: flex;
                justify-content: space-between;
                margin-bottom: 5px;
                font-size: 0.9rem;
            }

            .personal-stat span:first-child {
                color: rgba(255, 255, 255, 0.7);
            }

            .personal-stat span:last-child {
                color: white;
                font-weight: bold;
            }

            .action-buttons {
                display: flex;
                gap: 10px;
                flex-wrap: wrap;
            }

            .action-btn {
                flex: 1;
                min-width: 100px;
                background: linear-gradient(45deg, #667eea, #764ba2);
                border: none;
                color: white;
                padding: 12px 16px;
                border-radius: 12px;
                cursor: pointer;
                font-size: 0.85rem;
                font-weight: 500;
                transition: all 0.3s ease;
                text-align: center;
            }

            .action-btn:hover {
                transform: translateY(-2px);
                box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
            }

            .action-btn.secondary {
                background: rgba(255, 255, 255, 0.1);
                border: 1px solid rgba(255, 255, 255, 0.2);
            }

            .action-btn.secondary:hover {
                background: rgba(255, 255, 255, 0.2);
            }

            /* 통계 패널 */
            .stats-panel {
                position: fixed;
                top: 20px;
                right: 20px;
                width: 250px;
                background: rgba(0, 0, 0, 0.8);
                border: 1px solid rgba(255, 255, 255, 0.1);
                border-radius: 15px;
                padding: 20px;
                backdrop-filter: blur(20px);
                z-index: 100;
                transform: translateX(100%);
                transition: transform 0.3s ease;
            }

            .stats-panel.expanded {
                transform: translateX(0);
            }

            .stats-header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 15px;
            }

            .stats-header h3 {
                font-size: 1rem;
                color: #4ecdc4;
                margin: 0;
            }

            .stats-toggle {
                background: none;
                border: none;
                color: #4ecdc4;
                cursor: pointer;
                font-size: 1.2rem;
                padding: 5px;
                border-radius: 5px;
                transition: background 0.3s ease;
            }

            .stats-toggle:hover {
                background: rgba(78, 205, 196, 0.2);
            }

            .stat-item {
                display: flex;
                justify-content: space-between;
                margin-bottom: 10px;
                font-size: 0.9rem;
            }

            .stat-label {
                color: rgba(255, 255, 255, 0.7);
            }

            .stat-value {
                color: white;
                font-weight: bold;
            }

            /* 필터 리셋 버튼 */
            .filter-actions {
                margin-top: 15px;
                text-align: center;
            }

            .filter-reset-btn {
                background: rgba(255, 255, 255, 0.1);
                border: 1px solid rgba(255, 255, 255, 0.2);
                color: white;
                padding: 8px 16px;
                border-radius: 15px;
                cursor: pointer;
                font-size: 0.8rem;
                transition: all 0.3s ease;
            }

            .filter-reset-btn:hover {
                background: rgba(255, 255, 255, 0.2);
                transform: translateY(-1px);
            }

            /* 결과 없음 상태 */
            .no-results {
                position: absolute;
                top: 50%;
                left: 50%;
                transform: translate(-50%, -50%);
                text-align: center;
                color: white;
                z-index: 10;
            }

            .no-results-icon {
                font-size: 4rem;
                margin-bottom: 20px;
                opacity: 0.5;
            }

            .no-results-title {
                font-size: 1.5rem;
                font-weight: bold;
                margin-bottom: 10px;
            }

            .no-results-subtitle {
                font-size: 1rem;
                color: rgba(255, 255, 255, 0.7);
                margin-bottom: 20px;
            }

            .no-results-btn {
                background: linear-gradient(45deg, #667eea, #764ba2);
                border: none;
                color: white;
                padding: 12px 24px;
                border-radius: 20px;
                cursor: pointer;
                font-size: 1rem;
                transition: all 0.3s ease;
            }

            .no-results-btn:hover {
                transform: translateY(-2px);
                box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
            }

            /* 토스트 메시지 */
            .toast-message {
                position: fixed;
                top: 50%;
                left: 50%;
                transform: translate(-50%, -50%);
                background: rgba(0, 0, 0, 0.9);
                color: white;
                padding: 15px 25px;
                border-radius: 25px;
                font-size: 1rem;
                z-index: 1000;
                opacity: 0;
                transition: all 0.3s ease;
                backdrop-filter: blur(10px);
                border: 1px solid rgba(255, 255, 255, 0.2);
            }

            .toast-message.show {
                opacity: 1;
                transform: translate(-50%, -50%) scale(1.05);
            }

            /* 에러 상태 */
            .error-state {
                position: absolute;
                top: 50%;
                left: 50%;
                transform: translate(-50%, -50%);
                text-align: center;
                color: white;
                z-index: 10;
            }

            .error-icon {
                font-size: 4rem;
                margin-bottom: 20px;
                color: #e74c3c;
            }

            .error-title {
                font-size: 1.5rem;
                font-weight: bold;
                margin-bottom: 10px;
            }

            .error-message {
                font-size: 1rem;
                color: rgba(255, 255, 255, 0.7);
                margin-bottom: 20px;
            }

            .error-retry-btn {
                background: linear-gradient(45deg, #e74c3c, #c0392b);
                border: none;
                color: white;
                padding: 12px 24px;
                border-radius: 20px;
                cursor: pointer;
                font-size: 1rem;
                transition: all 0.3s ease;
            }

            .error-retry-btn:hover {
                transform: translateY(-2px);
                box-shadow: 0 8px 25px rgba(231, 76, 60, 0.4);
            }

            /* 반응형 디자인 */
            @media (max-width: 1200px) {
                .champion-card {
                    width: 160px;
                    height: 220px;
                    padding: 12px;
                }

                .orbit-ring.ring-1 { width: 400px; height: 400px; }
                .orbit-ring.ring-2 { width: 600px; height: 600px; }
                .orbit-ring.ring-3 { width: 800px; height: 800px; }

                @keyframes orbit-tier-1 {
                    from { transform: translate(-50%, -50%) rotate(0deg) translateX(200px) rotate(0deg); }
                    to { transform: translate(-50%, -50%) rotate(360deg) translateX(200px) rotate(-360deg); }
                }

                @keyframes orbit-tier-2 {
                    from { transform: translate(-50%, -50%) rotate(0deg) translateX(300px) rotate(0deg); }
                    to { transform: translate(-50%, -50%) rotate(360deg) translateX(300px) rotate(-360deg); }
                }

                @keyframes orbit-tier-3 {
                    from { transform: translate(-50%, -50%) rotate(0deg) translateX(400px) rotate(0deg); }
                    to { transform: translate(-50%, -50%) rotate(360deg) translateX(400px) rotate(-360deg); }
                }
            }

            @media (max-width: 768px) {
                .galaxy-header {
                    position: static;
                    transform: none;
                    margin: 20px;
                    padding: 15px;
                }

                .galaxy-title {
                    font-size: 1.5rem;
                }

                .champion-card {
                    width: 140px;
                    height: 200px;
                    padding: 10px;
                }

                .player-hub {
                    width: 150px;
                    height: 150px;
                }

                .player-info {
                    width: 130px;
                    height: 130px;
                }

                .detail-panel {
                    position: fixed;
                    bottom: 0;
                    left: 0;
                    right: 0;
                    width: auto;
                    max-height: 70vh;
                    border-radius: 20px 20px 0 0;
                }

                .filter-controls {
                    position: static;
                    margin: 20px;
                    width: calc(100% - 40px);
                }

                .stats-panel {
                    position: static;
                    margin: 20px;
                    width: calc(100% - 40px);
                    transform: none;
                }

                .orbit-ring.ring-1 { width: 300px; height: 300px; }
                .orbit-ring.ring-2 { width: 450px; height: 450px; }
                .orbit-ring.ring-3 { width: 600px; height: 600px; }

                @keyframes orbit-tier-1 {
                    from { transform: translate(-50%, -50%) rotate(0deg) translateX(150px) rotate(0deg); }
                    to { transform: translate(-50%, -50%) rotate(360deg) translateX(150px) rotate(-360deg); }
                }

                @keyframes orbit-tier-2 {
                    from { transform: translate(-50%, -50%) rotate(0deg) translateX(225px) rotate(0deg); }
                    to { transform: translate(-50%, -50%) rotate(360deg) translateX(225px) rotate(-360deg); }
                }

                @keyframes orbit-tier-3 {
                    from { transform: translate(-50%, -50%) rotate(0deg) translateX(300px) rotate(0deg); }
                    to { transform: translate(-50%, -50%) rotate(360deg) translateX(300px) rotate(-360deg); }
                }
            }
        `;

        document.head.appendChild(style);
    }

    // ===== 키보드 단축키 처리 =====
    handleKeydown(e) {
        switch(e.key) {
            case 'Escape':
                this.hideDetailPanel();
                break;
            case '1':
                this.filterByRole('TOP');
                break;
            case '2':
                this.filterByRole('JUNGLE');
                break;
            case '3':
                this.filterByRole('MID');
                break;
            case '4':
                this.filterByRole('ADC');
                break;
            case '5':
                this.filterByRole('SUPPORT');
                break;
            case '0':
                this.resetFilters();
                break;
        }
    }

    filterByRole(role) {
        const roleBtn = document.querySelector(`[data-role="${role}"]`);
        if (roleBtn) {
            roleBtn.click();
        }
    }

    handleResize() {
        // 윈도우 리사이즈 시 레이아웃 재조정
        this.renderChampions();
    }
}

// ===== 전역 변수로 시스템 인스턴스 저장 =====
let galaxySystem = null;

// ===== 시스템 초기화 함수 (personal.html에서 호출) =====
window.initializeCardsGalaxy = function(championRecommendations, playerData, currentVersion) {
    console.log('🌌 Cards Galaxy 시스템 초기화 요청');
    
    try {
        galaxySystem = new CardsGalaxySystem(championRecommendations, playerData, currentVersion);
        
        // 전역에서 접근 가능하도록 설정
        window.galaxySystem = galaxySystem;
        
        console.log('✅ Cards Galaxy 시스템 초기화 완료');
        
    } catch (error) {
        console.error('❌ Cards Galaxy 시스템 초기화 실패:', error);
    }
};

// ===== 페이지 로드 시 자동 초기화 (백업) =====
document.addEventListener('DOMContentLoaded', function() {
    const container = document.getElementById('galaxyContainer');
    if (container && !galaxySystem) {
        console.log('🌌 DOMContentLoaded: Cards Galaxy 자동 초기화');
        
        // Thymeleaf 변수들 확인
        const championRecommendations = window.championRecommendations || null;
        const playerData = window.playerData || null;
        const currentVersion = window.currentVersion || '14.23.1';
        
        window.initializeCardsGalaxy(championRecommendations, playerData, currentVersion);
    }
});

// ===== 모듈 export (Node.js 환경 대응) =====
if (typeof module !== 'undefined' && module.exports) {
    module.exports = CardsGalaxySystem;
}🎨 UI 컴포넌트 초기화
     */
    initializeUI() {
        const container = document.getElementById('galaxyContainer');
        if (!container) {
            console.error('Galaxy container not found');
            return;
        }

        // Galaxy HTML 구조 생성
        container.innerHTML = `
            <!-- 배경 효과 -->
            <div class="stars"></div>
            <div class="cosmic-particles"></div>

            <!-- 로딩 오버레이 -->
            <div class="loading-overlay" id="loadingOverlay">
                <div class="loading-spinner"></div>
                <div class="loading-text">
                    AI가 당신의 플레이 스타일을 분석하고 있습니다...<br>
                    <small style="color: rgba(255,255,255,0.6); margin-top: 10px; display: block;">
                        실시간 메타 데이터와 개인 성향을 매칭 중 ⚡
                    </small>
                </div>
            </div>

            <!-- 헤더 -->
            <div class="galaxy-header">
                <h1 class="galaxy-title">🌌 Cards Galaxy</h1>
                <p class="galaxy-subtitle">AI-Powered Champion Recommendation System</p>
                <div class="player-info-badge">
                    <span class="player-name">${this.playerData.name}</span>
                    <span class="player-stats">${this.playerData.winRate}% WR • ${this.playerData.averageKDA} KDA</span>
                </div>
            </div>

            <!-- 궤도 링들 -->
            <div class="orbit-ring ring-1"></div>
            <div class="orbit-ring ring-2"></div>
            <div class="orbit-ring ring-3"></div>

            <!-- 플레이어 허브 (중앙) -->
            <div class="player-hub" id="playerHub">
                <div class="player-info">
                    <div class="player-avatar"></div>
                    <div class="player-name">${this.playerData.name}</div>
                    <div class="player-stats">${this.playerData.winRate}% WR • ${this.playerData.averageKDA} KDA</div>
                    <div class="player-tier">${this.playerData.tier || 'UNRANKED'}</div>
                </div>
                <div class="hub-pulse"></div>
            </div>

            <!-- 챔피언 카드들 컨테이너 -->
            <div id="championCards" class="champion-cards-container"></div>

            <!-- 필터 컨트롤 -->
            <div class="filter-controls">
                <div class="filter-title">🎯 Filters</div>

                <div class="filter-group">
                    <label class="filter-label">Role</label>
                    <div class="filter-buttons">
                        <button class="filter-btn active" data-role="ALL">All</button>
                        <button class="filter-btn" data-role="TOP">Top</button>
                        <button class="filter-btn" data-role="JUNGLE">Jungle</button>
                        <button class="filter-btn" data-role="MID">Mid</button>
                        <button class="filter-btn" data-role="ADC">ADC</button>
                        <button class="filter-btn" data-role="SUPPORT">Support</button>
                    </div>
                </div>

                <div class="filter-group">
                    <label class="filter-label">Difficulty</label>
                    <div class="filter-buttons">
                        <button class="filter-btn active" data-difficulty="ALL">All</button>
                        <button class="filter-btn" data-difficulty="쉬움">Easy</button>
                        <button class="filter-btn" data-difficulty="보통">Medium</button>
                        <button class="filter-btn" data-difficulty="어려움">Hard</button>
                    </div>
                </div>

                <div class="filter-group">
                    <label class="filter-label">Meta Tier</label>
                    <div class="filter-buttons">
                        <button class="filter-btn active" data-tier="ALL">All</button>
                        <button class="filter-btn" data-tier="S">S-Tier</button>
                        <button class="filter-btn" data-tier="A">A-Tier</button>
                        <button class="filter-btn" data-tier="B">B-Tier</button>
                    </div>
                </div>

                <div class="filter-actions">
                    <button class="filter-reset-btn" onclick="galaxySystem.resetFilters()">
                        🔄 Reset Filters
                    </button>
                </div>
            </div>

            <!-- 상세 정보 패널 -->
            <div class="detail-panel" id="detailPanel">
                <div class="detail-header">
                    <div class="detail-avatar" id="detailAvatar"></div>
                    <div class="detail-info">
                        <h3 id="detailChampionName">Champion Name</h3>
                        <div class="detail-score" id="detailConfidence">95% Match</div>
                        <div class="detail-role" id="detailRole">MID</div>
                    </div>
                    <button class="detail-close-btn" onclick="galaxySystem.hideDetailPanel()">×</button>
                </div>

                <div class="detail-body">
                    <div class="recommendation-reasons" id="recommendationReasons">
                        <!-- 동적으로 생성됨 -->
                    </div>

                    <div class="personal-stats" id="personalStats" style="display: none;">
                        <!-- 개인 통계가 있을 때만 표시 -->
                    </div>

                    <div class="action-buttons">
                        <button class="action-btn primary" onclick="galaxySystem.openChampionGuide()">
                            📚 View Guide
                        </button>
                        <button class="action-btn secondary" onclick="galaxySystem.addToFavorites()">
                            ⭐ Add to Favorites
                        </button>
                        <button class="action-btn secondary" onclick="galaxySystem.shareTip()">
                            📤 Share Tip
                        </button>
                    </div>
                </div>
            </div>

            <!-- 통계 패널 -->
            <div class="stats-panel" id="statsPanel">
                <div class="stats-header">
                    <h3>📊 Recommendation Stats</h3>
                    <button class="stats-toggle" onclick="galaxySystem.toggleStatsPanel()">📈</button>
                </div>
                <div class="stats-content">
                    <div class="stat-item">
                        <span class="stat-label">Total Champions</span>
                        <span class="stat-value" id="totalChampions">0</span>
                    </div>
                    <div class="stat-item">
                        <span class="stat-label">Perfect Matches</span>
                        <span class="stat-value" id="perfectMatches">0</span>
                    </div>
                    <div class="stat-item">
                        <span class="stat-label">New Challenges</span>
                        <span class="stat-value" id="newChallenges">0</span>
                    </div>
                </div>
            </div>
        `;

        // CSS 스타일 동적 추가
        this.injectStyles();
    }

    /**
     * 📊 데이터 로딩
     */
    async loadChampionData() {
        try {
            if (this.championRecommendations && this.championRecommendations.recommendations) {
                // 백엔드에서 받은 추천 데이터 사용
                this.champions = this.championRecommendations.recommendations.map(rec => {
                    return {
                        name: rec.championName,
                        role: rec.primaryRole,
                        confidence: Math.round(rec.confidenceScore * 100),
                        difficulty: rec.difficulty,
                        tier: this.getMetaTier(rec.metaStrength),
                        tags: this.generateTags(rec),
                        reasons: rec.reasons || [],
                        styleMatch: Math.round(rec.styleMatch * 100),
                        skillMatch: Math.round(rec.skillMatch * 100),
                        type: rec.recommendationType,
                        metaStrength: rec.metaStrength,
                        personalStats: rec.personalStats
                    };
                });
            } else {
                // 폴백 데이터 사용
                this.champions = this.getDefaultChampions();
            }

            this.filteredChampions = [...this.champions];
            
            console.log(`📊 ${this.champions.length}개 챔피언 데이터 로드 완료`);
            
        } catch (error) {
            console.error('데이터 로딩 실패:', error);
            this.champions = this.getDefaultChampions();
            this.filteredChampions = [...this.champions];
        }
    }

    /**
     * 🎮 이벤트 리스너 설정
     */
    setupEventListeners() {
        // 필터 버튼 이벤트
        document.querySelectorAll('.filter-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const button = e.target;
                const filterType = this.getFilterType(button);
                const filterValue = button.dataset[filterType] || button.textContent.toUpperCase();

                // 같은 그룹의 다른 버튼들 비활성화
                const group = button.closest('.filter-group');
                group.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
                button.classList.add('active');

                // 필터 적용
                this.selectedFilters[filterType] = filterValue;
                this.applyFilters();
            });
        });

        // 마우스 이동에 따른 3D 효과
        document.addEventListener('mousemove', (e) => {
            this.handleMouseMove(e);
        });

        // 윈도우 리사이즈 대응
        window.addEventListener('resize', () => {
            this.handleResize();
        });

        // 키보드 이벤트 (필터링 단축키)
        document.addEventListener('keydown', (e) => {
            this.handleKeydown(e);
        });

        // 상세 패널 외부 클릭으로 닫기
        document.addEventListener('click', (e) => {
            if (!e.target.closest('.detail-panel') && !e.target.closest('.champion-card')) {
                this.hideDetailPanel();
            }
        });
    }

    /**
     * ✨ 애니메이션 시작
     */
    startAnimations() {
        // 별들 반짝임 애니메이션
        this.animateStars();
        
        // 궤도 링 회전
        this.animateOrbitRings();
        
        // 플레이어 허브 펄스
        this.animatePlayerHub();
        
        // 우주 파티클 애니메이션
        this.animateCosmicParticles();
    }

    /**
     *