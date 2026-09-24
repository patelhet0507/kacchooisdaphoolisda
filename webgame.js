/**
 * Kaachu Phool — Web Browser Engine (AI & Multiplayer)
 * Single-player AI and Real-time Firebase Multiplayer implementation.
 */

// Web Audio Synthesizer
class SoundManager {
  constructor() {
    this.ctx = null;
  }

  init() {
    if (!this.ctx) {
      const AudioCtx = window.AudioContext || window.webkitAudioContext;
      if (AudioCtx) this.ctx = new AudioCtx();
    }
    if (this.ctx && this.ctx.state === 'suspended') {
      this.ctx.resume();
    }
  }

  playClick() {
    this.init();
    if (!this.ctx) return;
    const osc = this.ctx.createOscillator();
    const gain = this.ctx.createGain();
    osc.type = 'sine';
    osc.frequency.setValueAtTime(600, this.ctx.currentTime);
    osc.frequency.exponentialRampToValueAtTime(200, this.ctx.currentTime + 0.08);
    gain.gain.setValueAtTime(0.15, this.ctx.currentTime);
    gain.gain.linearRampToValueAtTime(0.01, this.ctx.currentTime + 0.08);
    osc.connect(gain);
    gain.connect(this.ctx.destination);
    osc.start();
    osc.stop(this.ctx.currentTime + 0.08);
  }

  playCard() {
    this.init();
    if (!this.ctx) return;
    const bufferSize = this.ctx.sampleRate * 0.05;
    const buffer = this.ctx.createBuffer(1, bufferSize, this.ctx.sampleRate);
    const data = buffer.getChannelData(0);
    for (let i = 0; i < bufferSize; i++) {
      data[i] = (Math.random() * 2 - 1) * Math.exp(-i / (bufferSize * 0.2));
    }
    const noise = this.ctx.createBufferSource();
    noise.buffer = buffer;
    const filter = this.ctx.createBiquadFilter();
    filter.type = 'lowpass';
    filter.frequency.setValueAtTime(1200, this.ctx.currentTime);
    const gain = this.ctx.createGain();
    gain.gain.setValueAtTime(0.2, this.ctx.currentTime);
    noise.connect(filter);
    filter.connect(gain);
    gain.connect(this.ctx.destination);
    noise.start();
  }

  playTrickWin() {
    this.init();
    if (!this.ctx) return;
    const now = this.ctx.currentTime;
    [523.25, 659.25, 783.99].forEach((freq, idx) => {
      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();
      osc.type = 'triangle';
      osc.frequency.setValueAtTime(freq, now + idx * 0.06);
      gain.gain.setValueAtTime(0.15, now + idx * 0.06);
      gain.gain.exponentialRampToValueAtTime(0.001, now + idx * 0.06 + 0.2);
      osc.connect(gain);
      gain.connect(this.ctx.destination);
      osc.start(now + idx * 0.06);
      osc.stop(now + idx * 0.06 + 0.2);
    });
  }

  playVictory() {
    this.init();
    if (!this.ctx) return;
    const now = this.ctx.currentTime;
    [440, 554.37, 659.25, 880].forEach((freq, idx) => {
      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();
      osc.type = 'sine';
      osc.frequency.setValueAtTime(freq, now + idx * 0.12);
      gain.gain.setValueAtTime(0.2, now + idx * 0.12);
      gain.gain.exponentialRampToValueAtTime(0.001, now + idx * 0.12 + 0.4);
      osc.connect(gain);
      gain.connect(this.ctx.destination);
      osc.start(now + idx * 0.12);
      osc.stop(now + idx * 0.12 + 0.4);
    });
  }
}

const soundManager = new SoundManager();

// Game Data Constants
const SUITS = [
  { id: 'SPADES', symbol: '♠', name: 'Ka (Kali)', color: 'text-white', bg: 'bg-neutral-900' },
  { id: 'DIAMONDS', symbol: '♦', name: 'Chu (Chokat)', color: 'text-amber-400', bg: 'bg-neutral-900' },
  { id: 'CLUBS', symbol: '♣', name: 'Fu (Fuli)', color: 'text-white', bg: 'bg-neutral-900' },
  { id: 'HEARTS', symbol: '♥', name: 'L (Laal)', color: 'text-rose-400', bg: 'bg-rose-950' }
];

const RANKS = [
  { id: '2', val: 2, label: '2' },
  { id: '3', val: 3, label: '3' },
  { id: '4', val: 4, label: '4' },
  { id: '5', val: 5, label: '5' },
  { id: '6', val: 6, label: '6' },
  { id: '7', val: 7, label: '7' },
  { id: '8', val: 8, label: '8' },
  { id: '9', val: 9, label: '9' },
  { id: '10', val: 10, label: '10' },
  { id: 'J', val: 11, label: 'J' },
  { id: 'Q', val: 12, label: 'Q' },
  { id: 'K', val: 13, label: 'K' },
  { id: 'A', val: 14, label: 'A' }
];

const GAME_MODES = {
  QUICK: { title: 'Quick Match', rounds: [1, 2, 3, 4, 5, 4, 3, 2, 1] },
  CLASSIC: { title: 'Classic (Standard)', rounds: [1, 2, 3, 4, 5, 6, 7, 8, 7, 6, 5, 4, 3, 2, 1] },
  ASCENDING: { title: 'Ascending Ladder', rounds: [1, 2, 3, 4, 5, 6, 7, 8] },
  FULL: { title: 'Full 10-Card Ladder', rounds: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1] }
};

const DEFAULT_BOTS = [
  { id: 'bot_1', name: 'Aarav (Pro)', avatar: '🦁', isBot: true },
  { id: 'bot_2', name: 'Priya (Master)', avatar: '🌸', isBot: true },
  { id: 'bot_3', name: 'Rohan (Ace)', avatar: '⚡', isBot: true }
];

// Global Web Game State Object
window.KaachuPhoolWeb = {
  mode: 'SINGLE_PLAYER', // 'SINGLE_PLAYER' or 'MULTIPLAYER'
  gameConfig: {
    playerName: 'Player 1',
    playerAvatar: '🦁',
    gameMode: 'QUICK',
    scoringRule: 'STANDARD', // 'STANDARD', 'PENALTY', 'BONUS'
    botDifficulty: 'MEDIUM'
  },
  
  // Active Game State
  players: [],
  roundIndex: 0,
  roundsSequence: [],
  dealerIndex: 0,
  currentTurnIndex: 0,
  trumpSuit: null,
  leadSuit: null,
  dealtHands: {}, // playerId -> card array
  bids: {}, // playerId -> Int
  tricksWon: {}, // playerId -> Int
  scores: {}, // playerId -> Int
  scoresHistory: [], // array of round score snapshots
  currentTrickCards: [], // array of { playerId, card }
  trickWinnerMessage: '',
  isBiddingPhase: false,
  isTrickFinished: false,
  
  // Firebase Multiplayer
  rtdb: null,
  roomCode: null,
  isHost: false,
  unsubscribeRoom: null,

  init() {
    this.setupFirebase();
    this.attachEventListeners();
  },

  setupFirebase() {
    try {
      if (window.firebase && !window.firebase.apps.length) {
        window.firebase.initializeApp({
          databaseURL: "https://gen-lang-client-0782479965-default-rtdb.asia-southeast1.firebasedatabase.app"
        });
      }
      if (window.firebase) {
        this.rtdb = window.firebase.database();
      }
    } catch (e) {
      console.warn("Firebase RTDB init notice:", e);
    }
  },

  attachEventListeners() {
    const startSingleBtn = document.getElementById('webStartSinglePlayerBtn');
    const createRoomBtn = document.getElementById('webCreateRoomBtn');
    const joinRoomBtn = document.getElementById('webJoinRoomBtn');
    const tabSingleBtn = document.getElementById('webTabSingleBtn');
    const tabMultiBtn = document.getElementById('webTabMultiBtn');
    const toggleScorecardBtn = document.getElementById('webToggleScorecardBtn');
    const closeScorecardBtn = document.getElementById('webCloseScorecardBtn');
    const resetGameBtn = document.getElementById('webResetGameBtn');

    tabSingleBtn?.addEventListener('click', () => this.switchTab('SINGLE'));
    tabMultiBtn?.addEventListener('click', () => this.switchTab('MULTI'));

    startSingleBtn?.addEventListener('click', () => {
      soundManager.playClick();
      this.startSinglePlayerGame();
    });

    createRoomBtn?.addEventListener('click', () => {
      soundManager.playClick();
      this.createMultiplayerRoom();
    });

    joinRoomBtn?.addEventListener('click', () => {
      soundManager.playClick();
      this.joinMultiplayerRoom();
    });

    toggleScorecardBtn?.addEventListener('click', () => {
      soundManager.playClick();
      const modal = document.getElementById('webScorecardModal');
      if (modal) modal.classList.remove('hidden');
    });

    closeScorecardBtn?.addEventListener('click', () => {
      soundManager.playClick();
      const modal = document.getElementById('webScorecardModal');
      if (modal) modal.classList.add('hidden');
    });

    resetGameBtn?.addEventListener('click', () => {
      soundManager.playClick();
      this.showSetupScreen();
    });
  },

  switchTab(type) {
    soundManager.playClick();
    const singlePanel = document.getElementById('webSinglePanel');
    const multiPanel = document.getElementById('webMultiPanel');
    const tabSingleBtn = document.getElementById('webTabSingleBtn');
    const tabMultiBtn = document.getElementById('webTabMultiBtn');

    if (type === 'SINGLE') {
      singlePanel?.classList.remove('hidden');
      multiPanel?.classList.add('hidden');
      tabSingleBtn?.classList.add('bg-goldPrimary', 'text-black');
      tabSingleBtn?.classList.remove('bg-emeraldCard', 'text-white');
      tabMultiBtn?.classList.remove('bg-goldPrimary', 'text-black');
      tabMultiBtn?.classList.add('bg-emeraldCard', 'text-white');
    } else {
      singlePanel?.classList.add('hidden');
      multiPanel?.classList.remove('hidden');
      tabMultiBtn?.classList.add('bg-goldPrimary', 'text-black');
      tabMultiBtn?.classList.remove('bg-emeraldCard', 'text-white');
      tabSingleBtn?.classList.remove('bg-goldPrimary', 'text-black');
      tabSingleBtn?.classList.add('bg-emeraldCard', 'text-white');
    }
  },

  showSetupScreen() {
    document.getElementById('webSetupSection')?.classList.remove('hidden');
    document.getElementById('webGameTableSection')?.classList.add('hidden');
    document.getElementById('webGameOverOverlay')?.classList.add('hidden');
  },

  startSinglePlayerGame() {
    const nameInput = document.getElementById('webPlayerNameInput');
    const avatarSel = document.getElementById('webAvatarSelect');
    const modeSel = document.getElementById('webModeSelect');
    const ruleSel = document.getElementById('webScoringSelect');
    const diffSel = document.getElementById('webDiffSelect');

    this.mode = 'SINGLE_PLAYER';
    this.gameConfig.playerName = nameInput?.value.trim() || 'Player 1';
    this.gameConfig.playerAvatar = avatarSel?.value || '🦁';
    this.gameConfig.gameMode = modeSel?.value || 'QUICK';
    this.gameConfig.scoringRule = ruleSel?.value || 'STANDARD';
    this.gameConfig.botDifficulty = diffSel?.value || 'MEDIUM';

    // Setup 4 Players
    this.players = [
      { id: 'user_local', name: this.gameConfig.playerName, avatar: this.gameConfig.playerAvatar, isBot: false },
      ...DEFAULT_BOTS
    ];

    this.roundsSequence = GAME_MODES[this.gameConfig.gameMode].rounds;
    this.roundIndex = 0;
    this.dealerIndex = 0;
    this.scores = {};
    this.players.forEach(p => this.scores[p.id] = 0);
    this.scoresHistory = [];

    document.getElementById('webSetupSection')?.classList.add('hidden');
    document.getElementById('webGameTableSection')?.classList.remove('hidden');

    this.startRound();
  },

  startRound() {
    const cardsCount = this.roundsSequence[this.roundIndex];
    // Trump rotation: Spades -> Diamonds -> Clubs -> Hearts -> Spades...
    const trumpSuitObj = SUITS[this.roundIndex % 4];
    this.trumpSuit = trumpSuitObj.id;
    this.leadSuit = null;
    this.currentTrickCards = [];
    this.isTrickFinished = false;

    // Reset round counters
    this.bids = {};
    this.tricksWon = {};
    this.players.forEach(p => {
      this.tricksWon[p.id] = 0;
    });

    // Create & Shuffle 52-card Deck
    const deck = [];
    SUITS.forEach(s => {
      RANKS.forEach(r => {
        deck.push({ suit: s.id, rank: r.id, val: r.val, symbol: s.symbol, label: r.label });
      });
    });

    // Fisher-Yates Shuffle
    for (let i = deck.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [deck[i], deck[j]] = [deck[j], deck[i]];
    }

    // Deal cards
    this.dealtHands = {};
    this.players.forEach((p, idx) => {
      const hand = deck.slice(idx * cardsCount, (idx + 1) * cardsCount);
      // Sort hand by suit and val
      hand.sort((a, b) => (a.suit === b.suit ? b.val - a.val : a.suit.localeCompare(b.suit)));
      this.dealtHands[p.id] = hand;
    });

    // Set turn to player after dealer
    this.currentTurnIndex = (this.dealerIndex + 1) % this.players.length;
    this.isBiddingPhase = true;

    this.renderTableUI();
    this.processBiddingTurn();
  },

  processBiddingTurn() {
    if (!this.isBiddingPhase) return;

    const currentPlayer = this.players[this.currentTurnIndex];
    const cardsCount = this.roundsSequence[this.roundIndex];

    if (currentPlayer.isBot) {
      setTimeout(() => {
        const botBid = this.calculateBotBid(currentPlayer.id, cardsCount);
        this.bids[currentPlayer.id] = botBid;
        soundManager.playClick();
        this.advanceBidding();
      }, 700);
    } else {
      // User turn to bid -> show bid modal / buttons
      this.showUserBiddingControls(cardsCount);
    }
  },

  calculateBotBid(botId, cardsCount) {
    const hand = this.dealtHands[botId] || [];
    let expectedTricks = 0;

    hand.forEach(c => {
      if (c.suit === this.trumpSuit) {
        expectedTricks += c.val >= 10 ? 1 : 0.6;
      } else if (c.val >= 13) { // Ace or King
        expectedTricks += 0.7;
      }
    });

    let bid = Math.round(expectedTricks);
    return Math.min(cardsCount, Math.max(0, bid));
  },

  showUserBiddingControls(maxCards) {
    const container = document.getElementById('webBiddingControls');
    if (!container) return;
    container.innerHTML = '';
    container.classList.remove('hidden');

    const label = document.createElement('div');
    label.className = 'w-full text-center text-xs font-bold text-goldPrimary font-mono mb-2 animate-pulse';
    label.textContent = `YOUR TURN TO BID (Round ${this.roundIndex + 1} • ${maxCards} Cards)`;
    container.appendChild(label);

    const btnRow = document.createElement('div');
    btnRow.className = 'flex flex-wrap items-center justify-center gap-2';

    for (let i = 0; i <= maxCards; i++) {
      const btn = document.createElement('button');
      btn.className = 'w-10 h-10 sm:w-12 sm:h-12 rounded-xl bg-goldPrimary hover:bg-goldLight text-black font-bold font-display text-base shadow-lg transition-all transform hover:scale-110';
      btn.textContent = i;
      btn.onclick = () => {
        soundManager.playClick();
        this.bids['user_local'] = i;
        container.classList.add('hidden');
        this.advanceBidding();
      };
      btnRow.appendChild(btn);
    }
    container.appendChild(btnRow);
  },

  advanceBidding() {
    this.renderTableUI();

    // Check if all players have bid
    if (Object.keys(this.bids).length === this.players.length) {
      this.isBiddingPhase = false;
      // Start playing phase
      this.currentTurnIndex = (this.dealerIndex + 1) % this.players.length;
      this.renderTableUI();
      this.processPlayingTurn();
    } else {
      this.currentTurnIndex = (this.currentTurnIndex + 1) % this.players.length;
      this.renderTableUI();
      this.processBiddingTurn();
    }
  },

  processPlayingTurn() {
    if (this.isBiddingPhase || this.isTrickFinished) return;

    const currentPlayer = this.players[this.currentTurnIndex];

    if (currentPlayer.isBot) {
      setTimeout(() => {
        const playedCard = this.selectBotCard(currentPlayer.id);
        this.playCard(currentPlayer.id, playedCard);
      }, 800);
    } else {
      // User turn -> highlight playable cards
      this.renderTableUI();
    }
  },

  selectBotCard(botId) {
    const hand = this.dealtHands[botId] || [];
    const legalCards = this.getLegalCards(hand);

    if (legalCards.length === 0) return hand[0];

    // Simple AI heuristic
    if (!this.leadSuit) {
      // Leading: play highest card if holds Ace/King or highest trump
      return legalCards[0];
    } else {
      // Following: if can win trick, play winning card, else play lowest
      return legalCards[legalCards.length - 1];
    }
  },

  getLegalCards(hand) {
    if (!this.leadSuit) return hand;
    const sameSuitCards = hand.filter(c => c.suit === this.leadSuit);
    return sameSuitCards.length > 0 ? sameSuitCards : hand;
  },

  playCard(playerId, card) {
    soundManager.playCard();

    // Remove card from hand
    const hand = this.dealtHands[playerId];
    const idx = hand.findIndex(c => c.suit === card.suit && c.rank === card.rank);
    if (idx !== -1) hand.splice(idx, 1);

    // Set lead suit if first card in trick
    if (this.currentTrickCards.length === 0) {
      this.leadSuit = card.suit;
    }

    this.currentTrickCards.push({ playerId, card });
    this.renderTableUI();

    // Check if trick complete
    if (this.currentTrickCards.length === this.players.length) {
      this.isTrickFinished = true;
      setTimeout(() => this.evaluateTrickWinner(), 1200);
    } else {
      this.currentTurnIndex = (this.currentTurnIndex + 1) % this.players.length;
      this.renderTableUI();
      this.processPlayingTurn();
    }
  },

  evaluateTrickWinner() {
    let winningEntry = this.currentTrickCards[0];

    for (let i = 1; i < this.currentTrickCards.length; i++) {
      const candidate = this.currentTrickCards[i];
      const winCard = winningEntry.card;
      const candCard = candidate.card;

      if (candCard.suit === this.trumpSuit && winCard.suit !== this.trumpSuit) {
        winningEntry = candidate;
      } else if (candCard.suit === winCard.suit && candCard.val > winCard.val) {
        winningEntry = candidate;
      }
    }

    const winner = this.players.find(p => p.id === winningEntry.playerId);
    this.tricksWon[winner.id] = (this.tricksWon[winner.id] || 0) + 1;
    this.trickWinnerMessage = `${winner.name} won the trick!`;
    soundManager.playTrickWin();

    this.renderTableUI();

    setTimeout(() => {
      this.currentTrickCards = [];
      this.leadSuit = null;
      this.isTrickFinished = false;
      this.trickWinnerMessage = '';

      // Check if round complete (no cards left)
      const userHand = this.dealtHands['user_local'] || [];
      if (userHand.length === 0) {
        this.finishRound();
      } else {
        // Winner leads next trick
        this.currentTurnIndex = this.players.findIndex(p => p.id === winner.id);
        this.renderTableUI();
        this.processPlayingTurn();
      }
    }, 1500);
  },

  finishRound() {
    // Calculate round scores
    const roundScores = {};
    const rule = this.gameConfig.scoringRule;

    this.players.forEach(p => {
      const bid = this.bids[p.id] || 0;
      const won = this.tricksWon[p.id] || 0;
      let pts = 0;

      if (rule === 'STANDARD') {
        pts = (bid === won) ? (10 + bid) : 0;
      } else if (rule === 'PENALTY') {
        pts = (bid === won) ? (10 + bid) : (-10 - bid);
      } else if (rule === 'BONUS') {
        pts = won + ((bid === won) ? 10 : 0);
      }

      roundScores[p.id] = pts;
      this.scores[p.id] = (this.scores[p.id] || 0) + pts;
    });

    this.scoresHistory.push({
      round: this.roundIndex + 1,
      cardsCount: this.roundsSequence[this.roundIndex],
      scores: { ...roundScores },
      totals: { ...this.scores }
    });

    this.renderScorecardModal();

    this.roundIndex++;
    if (this.roundIndex >= this.roundsSequence.length) {
      // Game Over!
      soundManager.playVictory();
      this.showGameOverOverlay();
    } else {
      this.dealerIndex = (this.dealerIndex + 1) % this.players.length;
      this.startRound();
    }
  },

  showGameOverOverlay() {
    const overlay = document.getElementById('webGameOverOverlay');
    const winnerText = document.getElementById('webGameOverWinnerText');
    const statsList = document.getElementById('webGameOverStatsList');

    if (!overlay) return;

    let highestScore = -9999;
    let winner = this.players[0];

    this.players.forEach(p => {
      if ((this.scores[p.id] || 0) > highestScore) {
        highestScore = this.scores[p.id];
        winner = p;
      }
    });

    if (winnerText) {
      winnerText.textContent = `${winner.avatar} ${winner.name} Wins! (${highestScore} pts)`;
    }

    if (statsList) {
      statsList.innerHTML = this.players.map(p => `
        <div class="flex items-center justify-between p-3 rounded-xl bg-black/40 border border-goldPrimary/30">
          <span class="font-bold">${p.avatar} ${p.name}</span>
          <span class="font-mono text-goldPrimary font-bold text-lg">${this.scores[p.id] || 0} pts</span>
        </div>
      `).join('');
    }

    overlay.classList.remove('hidden');
  },

  renderTableUI() {
    // Render round & trump header
    const roundLabel = document.getElementById('webTableRoundLabel');
    const trumpLabel = document.getElementById('webTableTrumpLabel');
    const statusMsg = document.getElementById('webTableStatusMsg');

    const cardsCount = this.roundsSequence[this.roundIndex] || 1;
    const trumpObj = SUITS.find(s => s.id === this.trumpSuit) || SUITS[0];

    if (roundLabel) roundLabel.textContent = `Round ${this.roundIndex + 1} / ${this.roundsSequence.length} (${cardsCount} Cards)`;
    if (trumpLabel) {
      trumpLabel.innerHTML = `<span class="${trumpObj.color} text-lg font-bold mr-1">${trumpObj.symbol}</span> ${trumpObj.name}`;
    }

    if (statusMsg) {
      if (this.trickWinnerMessage) {
        statusMsg.textContent = this.trickWinnerMessage;
      } else if (this.isBiddingPhase) {
        const curPlayer = this.players[this.currentTurnIndex];
        statusMsg.textContent = `Bidding Phase: ${curPlayer.name}'s turn...`;
      } else {
        const curPlayer = this.players[this.currentTurnIndex];
        statusMsg.textContent = `Trick in progress: ${curPlayer.name}'s turn to play...`;
      }
    }

    // Render 4 Player Slots (Top, Right, Left, Bottom)
    // Local player is always at index 0 (Bottom)
    const positions = ['bottom', 'left', 'top', 'right'];
    this.players.forEach((p, idx) => {
      const pos = positions[idx];
      const nameEl = document.getElementById(`webPod_${pos}_name`);
      const bidEl = document.getElementById(`webPod_${pos}_bid`);
      const scoreEl = document.getElementById(`webPod_${pos}_score`);
      const podBox = document.getElementById(`webPod_${pos}_box`);

      if (nameEl) nameEl.textContent = `${p.avatar} ${p.name}`;
      if (bidEl) {
        const bid = this.bids[p.id] !== undefined ? this.bids[p.id] : '?';
        const won = this.tricksWon[p.id] || 0;
        bidEl.textContent = `Bid: ${bid} • Won: ${won}`;
      }
      if (scoreEl) scoreEl.textContent = `${this.scores[p.id] || 0} pts`;

      if (podBox) {
        if (idx === this.currentTurnIndex) {
          podBox.classList.add('border-goldPrimary', 'ring-2', 'ring-goldPrimary/50', 'bg-goldPrimary/10');
        } else {
          podBox.classList.remove('border-goldPrimary', 'ring-2', 'ring-goldPrimary/50', 'bg-goldPrimary/10');
        }
      }
    });

    // Render Center Played Trick Cards
    const trickContainer = document.getElementById('webCenterTrickCards');
    if (trickContainer) {
      trickContainer.innerHTML = '';
      this.currentTrickCards.forEach(item => {
        const suitObj = SUITS.find(s => s.id === item.card.suit) || SUITS[0];
        const cardDiv = document.createElement('div');
        cardDiv.className = `w-14 sm:w-16 h-20 sm:h-24 rounded-xl ${suitObj.bg} ${suitObj.border} border-2 p-2 flex flex-col justify-between shadow-2xl transform hover:scale-105 transition-all select-none animate-bounce-short`;
        cardDiv.innerHTML = `
          <div class="text-xs font-bold font-display ${suitObj.color}">${item.card.label}</div>
          <div class="text-center text-xl sm:text-2xl ${suitObj.color}">${item.card.symbol}</div>
          <div class="text-right text-[10px] ${suitObj.color}">${item.card.label}</div>
        `;
        trickContainer.appendChild(cardDiv);
      });
    }

    // Render Local Player's Hand
    const handContainer = document.getElementById('webUserHandContainer');
    if (handContainer) {
      handContainer.innerHTML = '';
      const userHand = this.dealtHands['user_local'] || [];
      const isUserTurn = !this.isBiddingPhase && !this.isTrickFinished && this.players[this.currentTurnIndex].id === 'user_local';
      const legalCards = this.getLegalCards(userHand);

      userHand.forEach(card => {
        const suitObj = SUITS.find(s => s.id === card.suit) || SUITS[0];
        const isLegal = isUserTurn && legalCards.some(lc => lc.suit === card.suit && lc.rank === card.rank);

        const cardBtn = document.createElement('button');
        cardBtn.className = `w-16 sm:w-20 h-24 sm:h-28 rounded-xl ${suitObj.bg} ${isLegal ? 'border-amber-400 border-2 cursor-pointer transform hover:-translate-y-3 hover:scale-110 shadow-goldPrimary/20' : 'border-neutral-700 border opacity-60 cursor-not-allowed'} p-2.5 flex flex-col justify-between shadow-xl transition-all select-none`;
        cardBtn.innerHTML = `
          <div class="flex justify-between items-center text-xs sm:text-sm font-bold font-display ${suitObj.color}">
            <span>${card.label}</span>
            <span>${card.symbol}</span>
          </div>
          <div class="text-center text-2xl sm:text-3xl ${suitObj.color}">${card.symbol}</div>
          <div class="text-right text-xs font-mono ${suitObj.color}">${card.label}</div>
        `;

        if (isLegal) {
          cardBtn.onclick = () => {
            this.playCard('user_local', card);
          };
        }

        handContainer.appendChild(cardBtn);
      });
    }
  },

  renderScorecardModal() {
    const tableBody = document.getElementById('webScorecardTableBody');
    if (!tableBody) return;

    tableBody.innerHTML = this.scoresHistory.map(h => `
      <tr class="border-b border-emeraldBorder/30 font-mono text-xs text-center">
        <td class="p-2 font-bold text-goldPrimary">R${h.round} (${h.cardsCount}c)</td>
        ${this.players.map(p => `
          <td class="p-2">
            <span class="${h.scores[p.id] >= 0 ? 'text-emerald-300' : 'text-rose-400'}">${h.scores[p.id]}</span>
            <span class="text-white/40 text-[10px] ml-1">(${h.totals[p.id]})</span>
          </td>
        `).join('')}
      </tr>
    `).join('');
  },

  // Multiplayer Engine Realtime Methods
  async createMultiplayerRoom() {
    if (!this.rtdb) {
      alert("Firebase Realtime Database is connecting... Please try again in 3 seconds.");
      this.setupFirebase();
      return;
    }

    const hostNameInput = document.getElementById('webMultiHostNameInput');
    const hostName = hostNameInput?.value.trim() || 'Host Player';

    const roomCode = Math.floor(100000 + Math.random() * 900000).toString();
    this.roomCode = roomCode;
    this.isHost = true;
    this.mode = 'MULTIPLAYER';

    const roomRef = this.rtdb.ref(`rooms/${roomCode}`);
    await roomRef.set({
      roomId: roomCode,
      hostName: hostName,
      players: [hostName],
      gameState: 'WAITING',
      createdAt: Date.now()
    });

    this.showLobbyUI(roomCode, hostName, [hostName]);
    this.listenMultiplayerRoom(roomCode);
  },

  async joinMultiplayerRoom() {
    if (!this.rtdb) {
      alert("Firebase connection initializing...");
      this.setupFirebase();
      return;
    }

    const codeInput = document.getElementById('webJoinRoomCodeInput');
    const nameInput = document.getElementById('webMultiJoinNameInput');
    const code = codeInput?.value.trim();
    const name = nameInput?.value.trim() || 'Guest Player';

    if (!code || code.length !== 6) {
      alert("Please enter a valid 6-digit room code.");
      return;
    }

    this.roomCode = code;
    this.isHost = false;
    this.mode = 'MULTIPLAYER';

    const roomRef = this.rtdb.ref(`rooms/${code}`);
    const snapshot = await roomRef.once('value');

    if (!snapshot.exists()) {
      alert("Room not found! Check the room code.");
      return;
    }

    const data = snapshot.val();
    const players = data.players || [];
    if (!players.includes(name)) {
      players.push(name);
      await roomRef.update({ players });
    }

    this.showLobbyUI(code, data.hostName, players);
    this.listenMultiplayerRoom(code);
  },

  showLobbyUI(code, hostName, players) {
    document.getElementById('webSetupSection')?.classList.add('hidden');
    document.getElementById('webLobbySection')?.classList.remove('hidden');

    const codeEl = document.getElementById('webLobbyCodeText');
    const playersEl = document.getElementById('webLobbyPlayersList');
    const startBtn = document.getElementById('webLobbyStartBtn');

    if (codeEl) codeEl.textContent = code;
    if (playersEl) {
      playersEl.innerHTML = players.map(p => `
        <div class="flex items-center gap-3 p-3 rounded-xl bg-black/40 border border-goldPrimary/30 font-bold text-sm">
          <span>👤</span>
          <span>${p}</span>
          ${p === hostName ? '<span class="text-xs bg-goldPrimary/20 text-goldPrimary px-2 py-0.5 rounded font-mono ml-auto">HOST</span>' : ''}
        </div>
      `).join('');
    }

    if (startBtn) {
      if (this.isHost) {
        startBtn.classList.remove('hidden');
        startBtn.onclick = () => {
          soundManager.playClick();
          alert("Multiplayer game session started! Syncing players...");
          this.rtdb.ref(`rooms/${code}`).update({ gameState: 'PLAYING' });
        };
      } else {
        startBtn.classList.add('hidden');
      }
    }
  },

  listenMultiplayerRoom(code) {
    if (!this.rtdb) return;
    const roomRef = this.rtdb.ref(`rooms/${code}`);

    roomRef.on('value', (snapshot) => {
      const data = snapshot.val();
      if (!data) return;

      if (data.gameState === 'PLAYING' && document.getElementById('webGameTableSection')?.classList.contains('hidden')) {
        document.getElementById('webLobbySection')?.classList.add('hidden');
        document.getElementById('webGameTableSection')?.classList.remove('hidden');
        // Start single/multi table sync
      } else if (data.players) {
        this.showLobbyUI(code, data.hostName, data.players);
      }
    });
  }
};

// Initialize on DOM Ready
document.addEventListener('DOMContentLoaded', () => {
  window.KaachuPhoolWeb.init();
});
