/**
 * Kaachu Phool — Web Browser Engine (AI & Real-Time Multiplayer)
 * Features:
 * - Real-Time Zero-Bot Human-Only Multiplayer (All joined players play together on 1 single room)
 * - Automatic Fullscreen Activation & Viewport Focus on Felt Table
 * - 30-Second Turn Countdown Timer & Circular Progress Bar around Active Player Avatar
 * - Compulsory Multiplayer Login & Player Profile Session System
 * - Ka-Chu-Fu-L Mnemonic Trump Rotation & Adaptive Player Pod Ergonomics (2, 3, or 4 players)
 * - Full Firebase Realtime Database Sync for Deal, Bids, Tricks, Emotes & Scorecards
 */

// Web Audio Synthesizer
class SoundManager {
  constructor() {
    this.ctx = null;
    this.muted = false;
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

  toggleMute() {
    this.muted = !this.muted;
    return this.muted;
  }

  playClick() {
    if (this.muted) return;
    this.init();
    if (!this.ctx) return;
    try {
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
    } catch (e) {}
  }

  playCard() {
    if (this.muted) return;
    this.init();
    if (!this.ctx) return;
    try {
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
      filter.frequency.setValueAtTime(1400, this.ctx.currentTime);
      const gain = this.ctx.createGain();
      gain.gain.setValueAtTime(0.2, this.ctx.currentTime);
      noise.connect(filter);
      filter.connect(gain);
      gain.connect(this.ctx.destination);
      noise.start();
    } catch (e) {}
  }

  playTrickWin() {
    if (this.muted) return;
    this.init();
    if (!this.ctx) return;
    try {
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
    } catch (e) {}
  }

  playVictory() {
    if (this.muted) return;
    this.init();
    if (!this.ctx) return;
    try {
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
    } catch (e) {}
  }

  playEmoji() {
    if (this.muted) return;
    this.init();
    if (!this.ctx) return;
    try {
      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();
      osc.type = 'sine';
      osc.frequency.setValueAtTime(400, this.ctx.currentTime);
      osc.frequency.exponentialRampToValueAtTime(800, this.ctx.currentTime + 0.15);
      gain.gain.setValueAtTime(0.15, this.ctx.currentTime);
      gain.gain.linearRampToValueAtTime(0.01, this.ctx.currentTime + 0.15);
      osc.connect(gain);
      gain.connect(this.ctx.destination);
      osc.start();
      osc.stop(this.ctx.currentTime + 0.15);
    } catch (e) {}
  }

  playWarningTick() {
    if (this.muted) return;
    this.init();
    if (!this.ctx) return;
    try {
      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();
      osc.type = 'square';
      osc.frequency.setValueAtTime(880, this.ctx.currentTime);
      gain.gain.setValueAtTime(0.08, this.ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime + 0.05);
      osc.connect(gain);
      gain.connect(this.ctx.destination);
      osc.start();
      osc.stop(this.ctx.currentTime + 0.05);
    } catch (e) {}
  }

  playChatSound() {
    if (this.muted) return;
    this.init();
    if (!this.ctx) return;
    try {
      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();
      osc.type = 'sine';
      osc.frequency.setValueAtTime(587.33, this.ctx.currentTime);
      osc.frequency.exponentialRampToValueAtTime(880, this.ctx.currentTime + 0.08);
      gain.gain.setValueAtTime(0.12, this.ctx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime + 0.08);
      osc.connect(gain);
      gain.connect(this.ctx.destination);
      osc.start();
      osc.stop(this.ctx.currentTime + 0.08);
    } catch (e) {}
  }
}

const soundManager = new SoundManager();

// Game Data Constants
const SUITS = [
  { id: 'SPADES', symbol: '♠', name: 'Ka (Kali)', color: 'text-amber-200', bg: 'bg-[#0f172a]', border: 'border-amber-300/40' },
  { id: 'DIAMONDS', symbol: '♦', name: 'Chu (Chokat)', color: 'text-amber-400', bg: 'bg-[#181308]', border: 'border-amber-400/50' },
  { id: 'CLUBS', symbol: '♣', name: 'Fu (Fuli)', color: 'text-emerald-300', bg: 'bg-[#062414]', border: 'border-emerald-400/50' },
  { id: 'HEARTS', symbol: '♥', name: 'L (Laal)', color: 'text-rose-400', bg: 'bg-[#25080c]', border: 'border-rose-500/50' }
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

// Global Web Game Engine Object
window.KaachuPhoolWeb = {
  mode: 'SINGLE_PLAYER', // 'SINGLE_PLAYER' or 'MULTIPLAYER'
  
  // Compulsory User Auth State
  currentUser: null, // { uid, displayName, email, avatar, rating, wins, matches }

  gameConfig: {
    playerName: 'Player 1',
    playerAvatar: '🦁',
    gameMode: 'QUICK',
    scoringRule: 'STANDARD', // 'STANDARD', 'PENALTY', 'BONUS'
    botDifficulty: 'MEDIUM'
  },
  
  // Active Game State
  players: [], // List of active human players (and bots if single-player)
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

  // Fast & Snappy Real-Time Pacing Engine
  gameSpeed: 'FAST', // 'FAST' (15s timer, snappy) or 'TURBO' (10s timer, instant)
  get delays() {
    if (this.gameSpeed === 'TURBO') {
      return {
        botBid: 90,
        botPlay: 120,
        trickEval: 220,
        trickClear: 320,
        multiplayerTrickClear: 400,
        turnTimeout: 10
      };
    }
    return {
      botBid: 160,
      botPlay: 200,
      trickEval: 300,
      trickClear: 500,
      multiplayerTrickClear: 550,
      turnTimeout: 15
    };
  },

  // Turn Countdown Timer State
  TURN_TIMEOUT_SEC: 15,
  turnTimeRemaining: 15,
  turnTimerInterval: null,
  
  // Slide-out In-Game Live Chat State
  isChatOpen: false,
  unreadChatCount: 0,
  chatRef: null,

  // Firebase Realtime Multiplayer
  rtdb: null,
  roomCode: null,
  isHost: false,
  multiplayerGameRef: null,
  multiplayerRoomRef: null,

  init() {
    this.loadPersistedAuth();
    this.setupFirebase();
    this.attachEventListeners();
    this.updateAuthUI();
  },

  loadPersistedAuth() {
    try {
      const stored = localStorage.getItem('kaachu_auth_user');
      if (stored) {
        this.currentUser = JSON.parse(stored);
      }
    } catch (e) {
      console.warn('Failed to load local auth session:', e);
    }
  },

  saveAuthSession(user) {
    this.currentUser = user;
    try {
      localStorage.setItem('kaachu_auth_user', JSON.stringify(user));
    } catch (e) {}
    this.updateAuthUI();
  },

  updateAuthUI() {
    const avatarBadge = document.getElementById('webMultiAuthAvatarBadge');
    const nameLabel = document.getElementById('webMultiAuthNameLabel');
    const statusPill = document.getElementById('webMultiAuthStatusPill');
    const detailLabel = document.getElementById('webMultiAuthDetailLabel');
    const openAuthBtnText = document.getElementById('webOpenAuthModalBtnText');
    const logoutBtn = document.getElementById('webMultiLogoutBtn');
    const hostInput = document.getElementById('webMultiHostNameInput');
    const joinInput = document.getElementById('webMultiJoinNameInput');

    if (this.currentUser) {
      // User is logged in
      if (avatarBadge) avatarBadge.textContent = this.currentUser.avatar || '👑';
      if (nameLabel) nameLabel.textContent = this.currentUser.displayName || 'Player';
      if (statusPill) {
        statusPill.textContent = 'VERIFIED PLAYER';
        statusPill.className = 'text-[10px] font-mono uppercase px-2 py-0.5 rounded-full bg-emerald-950 text-emerald-300 border border-emerald-500/40';
      }
      if (detailLabel) {
        detailLabel.textContent = `${this.currentUser.email || 'Online Account'} • 🏆 ELO: ${this.currentUser.rating || 1200} • ${this.currentUser.wins || 0} Wins`;
      }
      if (openAuthBtnText) openAuthBtnText.textContent = 'Account Info';
      if (logoutBtn) logoutBtn.classList.remove('hidden');

      if (hostInput) hostInput.value = this.currentUser.displayName;
      if (joinInput) joinInput.value = this.currentUser.displayName;
    } else {
      // User is NOT logged in (Login Compulsory for Multiplayer)
      if (avatarBadge) avatarBadge.textContent = '🔒';
      if (nameLabel) nameLabel.textContent = 'Guest Player';
      if (statusPill) {
        statusPill.textContent = 'Login Required';
        statusPill.className = 'text-[10px] font-mono uppercase px-2 py-0.5 rounded-full bg-rose-950 text-rose-300 border border-rose-500/40';
      }
      if (detailLabel) {
        detailLabel.textContent = 'Login is compulsory to create or join multiplayer matches.';
      }
      if (openAuthBtnText) openAuthBtnText.textContent = 'Sign In / Register';
      if (logoutBtn) logoutBtn.classList.add('hidden');
    }
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
    // Mode Switchers
    const tabSingleBtn = document.getElementById('webTabSingleBtn');
    const tabMultiBtn = document.getElementById('webTabMultiBtn');
    tabSingleBtn?.addEventListener('click', () => this.switchTab('SINGLE'));
    tabMultiBtn?.addEventListener('click', () => this.switchTab('MULTI'));

    // Single Player Launch
    const startSingleBtn = document.getElementById('webStartSinglePlayerBtn');
    startSingleBtn?.addEventListener('click', () => {
      soundManager.playClick();
      this.startSinglePlayerGame();
    });

    // Quick Join Single Shared Public Room (Room 777777)
    const quickJoinSingleBtn = document.getElementById('webQuickJoinSingleRoomBtn');
    quickJoinSingleBtn?.addEventListener('click', () => {
      soundManager.playClick();
      this.joinSingleSharedRoom();
    });

    // Multiplayer Room Creation & Joining
    const createRoomBtn = document.getElementById('webCreateRoomBtn');
    const joinRoomBtn = document.getElementById('webJoinRoomBtn');
    createRoomBtn?.addEventListener('click', () => {
      soundManager.playClick();
      this.createMultiplayerRoom();
    });
    joinRoomBtn?.addEventListener('click', () => {
      soundManager.playClick();
      this.joinMultiplayerRoom();
    });

    // All Play & Start triggers (Hero CTA, Navigation Links, etc.)
    document.querySelectorAll('[data-play-trigger], #heroPlayBtn, #navPlayBtn, #navPlayWebBtn, #mobileNavPlayBtn').forEach(btn => {
      btn.addEventListener('click', (e) => {
        if (btn.id === 'heroPlayBtn' || btn.classList.contains('play-cta-btn')) {
          e.preventDefault();
          soundManager.playClick();
          this.startSinglePlayerGame();
        } else {
          // If match is active, focus right back on the table
          const table = document.getElementById('webGameTableSection');
          if (table && !table.classList.contains('hidden')) {
            e.preventDefault();
            this.takeToTableAndFullscreen();
          }
        }
      });
    });

    // Fullscreen Change Listeners to keep icon synchronized
    ['fullscreenchange', 'webkitfullscreenchange', 'mozfullscreenchange', 'MSFullscreenChange'].forEach(evt => {
      document.addEventListener(evt, () => {
        const icon = document.getElementById('webFullscreenIcon');
        const isFull = !!(document.fullscreenElement || document.webkitFullscreenElement || document.mozFullScreenElement || document.msFullscreenElement);
        if (icon) {
          icon.textContent = isFull ? '🗕' : '⛶';
        }
      });
    });

    // Scorecard Modal
    const toggleScorecardBtn = document.getElementById('webToggleScorecardBtn');
    const closeScorecardBtn = document.getElementById('webCloseScorecardBtn');
    toggleScorecardBtn?.addEventListener('click', () => {
      soundManager.playClick();
      document.getElementById('webScorecardModal')?.classList.remove('hidden');
    });
    closeScorecardBtn?.addEventListener('click', () => {
      soundManager.playClick();
      document.getElementById('webScorecardModal')?.classList.add('hidden');
    });

    // Game Speed Toggle (Fast / Turbo)
    const speedToggleBtn = document.getElementById('webSpeedToggleBtn');
    speedToggleBtn?.addEventListener('click', () => {
      soundManager.playClick();
      this.toggleGameSpeed();
    });

    // Exit Game Button
    const resetGameBtn = document.getElementById('webResetGameBtn');
    resetGameBtn?.addEventListener('click', () => {
      soundManager.playClick();
      if (confirm("Leave current table and return to menu?")) {
        this.showSetupScreen();
      }
    });

    // Fullscreen Toggle
    const fullscreenBtn = document.getElementById('webFullscreenToggleBtn');
    fullscreenBtn?.addEventListener('click', () => {
      soundManager.playClick();
      this.toggleFullscreen();
    });

    // Sound Mute Toggle
    const soundToggleBtn = document.getElementById('webSoundToggleBtn');
    soundToggleBtn?.addEventListener('click', () => {
      const isMuted = soundManager.toggleMute();
      if (soundToggleBtn) soundToggleBtn.textContent = isMuted ? '🔇' : '🔊';
    });

    // Emoji Reactions Tray
    const emojiToggleBtn = document.getElementById('webEmojiTrayToggleBtn');
    emojiToggleBtn?.addEventListener('click', () => {
      soundManager.playClick();
      const tray = document.getElementById('webEmojiTray');
      tray?.classList.toggle('hidden');
    });

    document.querySelectorAll('.emoji-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        const emoji = btn.dataset.emoji;
        const myName = this.currentUser ? this.currentUser.displayName : 'You';
        this.triggerFloatingEmoji(emoji, myName);
        document.getElementById('webEmojiTray')?.classList.add('hidden');

        // Broadcast to multiplayer room if online
        if (this.mode === 'MULTIPLAYER' && this.rtdb && this.roomCode) {
          this.rtdb.ref(`rooms/${this.roomCode}/emotes`).set({
            emoji: emoji,
            senderName: myName,
            timestamp: Date.now()
          });
        }
      });
    });

    // Auth Modal Controls
    const openAuthBtn = document.getElementById('webOpenAuthModalBtn');
    const closeAuthBtn = document.getElementById('webCloseAuthModalBtn');
    const authModal = document.getElementById('webMultiAuthModal');
    openAuthBtn?.addEventListener('click', () => {
      soundManager.playClick();
      authModal?.classList.remove('hidden');
    });
    closeAuthBtn?.addEventListener('click', () => {
      soundManager.playClick();
      authModal?.classList.add('hidden');
    });

    // Auth Modal Tab Buttons
    const tabLoginBtn = document.getElementById('webAuthTabLoginBtn');
    const tabRegisterBtn = document.getElementById('webAuthTabRegisterBtn');
    const tabQuickBtn = document.getElementById('webAuthTabQuickBtn');
    const formLogin = document.getElementById('webLoginForm');
    const formRegister = document.getElementById('webRegisterForm');
    const formQuick = document.getElementById('webQuickAuthForm');

    tabLoginBtn?.addEventListener('click', () => {
      tabLoginBtn.className = 'flex-1 py-2 rounded-lg bg-goldPrimary text-black transition-all cursor-pointer';
      tabRegisterBtn.className = 'flex-1 py-2 rounded-lg text-white hover:text-goldPrimary transition-all cursor-pointer';
      tabQuickBtn.className = 'flex-1 py-2 rounded-lg text-white hover:text-goldPrimary transition-all cursor-pointer';
      formLogin?.classList.remove('hidden');
      formRegister?.classList.add('hidden');
      formQuick?.classList.add('hidden');
    });

    tabRegisterBtn?.addEventListener('click', () => {
      tabRegisterBtn.className = 'flex-1 py-2 rounded-lg bg-goldPrimary text-black transition-all cursor-pointer';
      tabLoginBtn.className = 'flex-1 py-2 rounded-lg text-white hover:text-goldPrimary transition-all cursor-pointer';
      tabQuickBtn.className = 'flex-1 py-2 rounded-lg text-white hover:text-goldPrimary transition-all cursor-pointer';
      formRegister?.classList.remove('hidden');
      formLogin?.classList.add('hidden');
      formQuick?.classList.add('hidden');
    });

    tabQuickBtn?.addEventListener('click', () => {
      tabQuickBtn.className = 'flex-1 py-2 rounded-lg bg-goldPrimary text-black transition-all cursor-pointer';
      tabLoginBtn.className = 'flex-1 py-2 rounded-lg text-white hover:text-goldPrimary transition-all cursor-pointer';
      tabRegisterBtn.className = 'flex-1 py-2 rounded-lg text-white hover:text-goldPrimary transition-all cursor-pointer';
      formQuick?.classList.remove('hidden');
      formLogin?.classList.add('hidden');
      formRegister?.classList.add('hidden');
    });

    // Login Form Submit
    formLogin?.addEventListener('submit', (e) => {
      e.preventDefault();
      soundManager.playClick();
      const email = document.getElementById('webLoginEmailInput')?.value.trim();
      const name = email.split('@')[0] || 'Player';
      const user = {
        uid: 'user_' + Math.random().toString(36).substr(2, 9),
        displayName: name.charAt(0).toUpperCase() + name.slice(1),
        email: email,
        avatar: '🦁',
        rating: 1200,
        wins: 3,
        matches: 8
      };
      this.saveAuthSession(user);
      authModal?.classList.add('hidden');
      alert(`Welcome back, ${user.displayName}! Multiplayer is now unlocked.`);
    });

    // Register Form Submit
    formRegister?.addEventListener('submit', (e) => {
      e.preventDefault();
      soundManager.playClick();
      const name = document.getElementById('webRegNameInput')?.value.trim();
      const avatar = document.getElementById('webRegAvatarSelect')?.value || '👑';
      const email = document.getElementById('webRegEmailInput')?.value.trim();
      const user = {
        uid: 'user_' + Math.random().toString(36).substr(2, 9),
        displayName: name,
        email: email,
        avatar: avatar,
        rating: 1200,
        wins: 0,
        matches: 0
      };
      this.saveAuthSession(user);
      authModal?.classList.add('hidden');
      alert(`Account created successfully! Welcome to Kaachu Phool Multiplayer, ${name}!`);
    });

    // Quick 1-Click Fast Guest Profile
    const instantGuestBtn = document.getElementById('webInstantGuestBtn');
    instantGuestBtn?.addEventListener('click', () => {
      soundManager.playClick();
      const randomNames = ['AcePlayer', 'KaachuKing', 'GujjuMaster', 'TrumpWizard', 'DesiHero', 'FuliAce', 'RoyalPlayer'];
      const chosenName = randomNames[Math.floor(Math.random() * randomNames.length)] + '_' + Math.floor(10 + Math.random() * 90);
      const avatars = ['🦁', '👑', '⚡', '💎', '🌸', '🐯', '🎴'];
      const chosenAvatar = avatars[Math.floor(Math.random() * avatars.length)];
      const user = {
        uid: 'user_' + Math.random().toString(36).substr(2, 9),
        displayName: chosenName,
        email: `${chosenName.toLowerCase()}@kaachuphool.game`,
        avatar: chosenAvatar,
        rating: 1200,
        wins: 0,
        matches: 0
      };
      this.saveAuthSession(user);
      authModal?.classList.add('hidden');
      alert(`Instant Verified Profile created: ${chosenAvatar} ${chosenName}`);
    });

    // Logout Button
    const logoutBtn = document.getElementById('webMultiLogoutBtn');
    logoutBtn?.addEventListener('click', () => {
      soundManager.playClick();
      if (confirm("Log out from your Multiplayer player profile?")) {
        this.currentUser = null;
        try { localStorage.removeItem('kaachu_auth_user'); } catch (e) {}
        this.updateAuthUI();
      }
    });

    // Copy Room Code Button in Lobby
    const copyCodeBtn = document.getElementById('webCopyLobbyCodeBtn');
    copyCodeBtn?.addEventListener('click', () => {
      soundManager.playClick();
      if (this.roomCode) {
        navigator.clipboard.writeText(this.roomCode);
        alert(`Room Code ${this.roomCode} copied to clipboard!`);
      }
    });

    // Lobby Leave Button
    const lobbyLeaveBtn = document.getElementById('webLobbyLeaveBtn');
    lobbyLeaveBtn?.addEventListener('click', () => {
      soundManager.playClick();
      this.leaveMultiplayerRoom();
      this.showSetupScreen();
    });

    // In-Game Live Chat Drawer Controls
    const chatToggleBtn = document.getElementById('webChatToggleBtn');
    const chatCloseBtn = document.getElementById('webCloseChatBtn');
    const chatBackdrop = document.getElementById('webGameChatBackdrop');
    const chatForm = document.getElementById('webChatInputForm');
    const chatInput = document.getElementById('webChatTextInput');

    chatToggleBtn?.addEventListener('click', () => {
      soundManager.playClick();
      this.toggleChatDrawer();
    });

    chatCloseBtn?.addEventListener('click', () => {
      soundManager.playClick();
      this.closeChatDrawer();
    });

    chatBackdrop?.addEventListener('click', () => {
      this.closeChatDrawer();
    });

    chatForm?.addEventListener('submit', (e) => {
      e.preventDefault();
      const text = chatInput?.value?.trim();
      if (text) {
        soundManager.playClick();
        this.sendChatMessage(text);
        if (chatInput) chatInput.value = '';
      }
    });

    document.querySelectorAll('.chat-quick-chip').forEach(btn => {
      btn.addEventListener('click', () => {
        const text = btn.textContent.trim();
        if (text) {
          soundManager.playClick();
          this.sendChatMessage(text);
        }
      });
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

      // Check if user is logged in
      if (!this.currentUser) {
        document.getElementById('webMultiAuthModal')?.classList.remove('hidden');
      }
    }
  },

  takeToTableAndFullscreen() {
    const table = document.getElementById('webGameTableSection');
    if (!table) return;

    // Reveal felt game table and hide background setup/lobby/overlay
    document.getElementById('webSetupSection')?.classList.add('hidden');
    document.getElementById('webLobbySection')?.classList.add('hidden');
    document.getElementById('webGameOverOverlay')?.classList.add('hidden');
    table.classList.remove('hidden');

    // Lock page scrolling strictly to full table view
    document.body.classList.add('overflow-hidden');
    document.documentElement.classList.add('overflow-hidden');
    window.scrollTo({ top: 0, left: 0, behavior: 'instant' });

    // Autofocus directly on the table container
    table.setAttribute('tabindex', '-1');
    table.style.outline = 'none';
    try {
      table.focus({ preventScroll: true });
    } catch (e) {
      table.focus();
    }

    // Trigger Fullscreen
    this.autoEnterFullscreen(table);
  },

  autoEnterFullscreen(targetEl = null) {
    const elem = targetEl || document.getElementById('webGameTableSection') || document.documentElement;
    try {
      const isFull = document.fullscreenElement || document.webkitFullscreenElement || document.mozFullScreenElement || document.msFullscreenElement;
      if (!isFull) {
        const req = elem.requestFullscreen ||
          elem.webkitRequestFullscreen ||
          elem.webkitRequestFullScreen ||
          elem.mozRequestFullScreen ||
          elem.msRequestFullscreen;

        if (req) {
          const promise = req.call(elem);
          if (promise && typeof promise.catch === 'function') {
            promise.catch((err) => {
              console.log("Fullscreen auto-enter notice (fallback overlay active):", err);
            });
          }
        }
      }
    } catch (e) {
      console.warn("Fullscreen trigger error:", e);
    }
  },

  toggleFullscreen() {
    const icon = document.getElementById('webFullscreenIcon');
    const isFull = document.fullscreenElement || document.webkitFullscreenElement || document.mozFullScreenElement || document.msFullscreenElement;
    const elem = document.getElementById('webGameTableSection') || document.documentElement;

    if (!isFull) {
      const req = elem.requestFullscreen || elem.webkitRequestFullscreen || elem.webkitRequestFullScreen || elem.mozRequestFullScreen || elem.msRequestFullscreen;
      if (req) {
        const promise = req.call(elem);
        if (promise && typeof promise.then === 'function') {
          promise.then(() => {
            if (icon) icon.textContent = '🗕';
          }).catch(err => {
            console.log("Fullscreen request fallback:", err);
          });
        }
      }
    } else {
      const exit = document.exitFullscreen || document.webkitExitFullscreen || document.mozCancelFullScreen || document.msExitFullscreen;
      if (exit) {
        const promise = exit.call(document);
        if (promise && typeof promise.then === 'function') {
          promise.then(() => {
            if (icon) icon.textContent = '⛶';
          }).catch(() => {});
        }
      }
    }
  },

  toggleGameSpeed() {
    this.gameSpeed = this.gameSpeed === 'FAST' ? 'TURBO' : 'FAST';
    const label = document.getElementById('webSpeedLabel');
    const icon = document.getElementById('webSpeedIcon');
    const btn = document.getElementById('webSpeedToggleBtn');
    if (label) label.textContent = this.gameSpeed === 'TURBO' ? 'Turbo' : 'Fast';
    if (icon) icon.textContent = this.gameSpeed === 'TURBO' ? '⚡⚡' : '⚡';
    if (btn) {
      if (this.gameSpeed === 'TURBO') {
        btn.className = 'px-2 sm:px-2.5 py-1.5 rounded-xl bg-amber-400 text-black border border-amber-300 text-xs font-mono font-bold hover:bg-amber-300 transition-all cursor-pointer flex items-center gap-1 shadow-lg';
      } else {
        btn.className = 'px-2 sm:px-2.5 py-1.5 rounded-xl bg-amber-500/20 border border-amber-400/50 text-amber-300 text-xs font-mono font-bold hover:bg-amber-500/30 transition-all cursor-pointer flex items-center gap-1';
      }
    }
    this.triggerFloatingEmoji(this.gameSpeed === 'TURBO' ? '⚡' : '⏩', `Game Pace: ${this.gameSpeed}`);
  },

  triggerFloatingEmoji(emoji, senderName) {
    soundManager.playEmoji();
    const container = document.getElementById('webFloatingEmojiContainer');
    if (!container) return;

    const el = document.createElement('div');
    el.className = 'absolute bottom-16 left-1/2 -translate-x-1/2 flex flex-col items-center pointer-events-none transition-all duration-1000 transform animate-floatUp z-50';
    el.innerHTML = `
      <div class="text-4xl filter drop-shadow-lg">${emoji}</div>
      <div class="text-[10px] font-mono text-goldPrimary bg-black/80 px-2 py-0.5 rounded-full border border-goldPrimary/40">${senderName}</div>
    `;
    container.appendChild(el);

    setTimeout(() => {
      el.remove();
    }, 2000);
  },

  showSetupScreen() {
    this.stopTurnTimer();
    this.closeChatDrawer();
    this.resetChat();
    // Restore normal window scrolling
    document.body.classList.remove('overflow-hidden');
    document.documentElement.classList.remove('overflow-hidden');

    document.getElementById('webSetupSection')?.classList.remove('hidden');
    document.getElementById('webLobbySection')?.classList.add('hidden');
    document.getElementById('webGameTableSection')?.classList.add('hidden');
    document.getElementById('webGameOverOverlay')?.classList.add('hidden');
    document.getElementById('webScorecardModal')?.classList.add('hidden');
  },

  // Slide-out Chat Drawer Methods
  toggleChatDrawer() {
    if (this.isChatOpen) {
      this.closeChatDrawer();
    } else {
      this.openChatDrawer();
    }
  },

  openChatDrawer() {
    this.isChatOpen = true;
    const drawer = document.getElementById('webGameChatDrawer');
    const backdrop = document.getElementById('webGameChatBackdrop');
    if (drawer) drawer.classList.remove('translate-x-full');
    if (backdrop) backdrop.classList.remove('hidden');

    // Clear unread badge
    this.unreadChatCount = 0;
    const unreadBadge = document.getElementById('webChatUnreadBadge');
    if (unreadBadge) {
      unreadBadge.classList.add('hidden');
      unreadBadge.textContent = '0';
    }

    const input = document.getElementById('webChatTextInput');
    setTimeout(() => input?.focus(), 150);
  },

  closeChatDrawer() {
    this.isChatOpen = false;
    const drawer = document.getElementById('webGameChatDrawer');
    const backdrop = document.getElementById('webGameChatBackdrop');
    if (drawer) drawer.classList.add('translate-x-full');
    if (backdrop) backdrop.classList.add('hidden');
  },

  resetChat() {
    this.unreadChatCount = 0;
    const unreadBadge = document.getElementById('webChatUnreadBadge');
    if (unreadBadge) {
      unreadBadge.classList.add('hidden');
      unreadBadge.textContent = '0';
    }
    const list = document.getElementById('webChatMessagesList');
    if (list) {
      list.innerHTML = `
        <div class="text-center text-[10px] font-mono text-emerald-300/60 py-2 border-b border-emeraldBorder/20">
          🎴 Table chat connected. Send messages to all players!
        </div>
      `;
    }
  },

  sendChatMessage(text) {
    if (!text || !text.trim()) return;
    const trimmed = text.trim().slice(0, 120);

    const myUid = this.currentUser ? this.currentUser.uid : 'user_local';
    const myName = this.currentUser ? this.currentUser.displayName : (this.gameConfig.playerName || 'Player');
    const myAvatar = this.currentUser ? this.currentUser.avatar : (this.gameConfig.playerAvatar || '🦁');

    const msgPayload = {
      senderId: myUid,
      senderName: myName,
      senderAvatar: myAvatar,
      message: trimmed,
      timestamp: Date.now()
    };

    if (this.mode === 'MULTIPLAYER' && this.rtdb && this.roomCode) {
      this.rtdb.ref(`rooms/${this.roomCode}/chat`).push(msgPayload);
    } else {
      // Single Player Mode - local message + friendly bot response
      this.appendChatMessage(msgPayload);

      setTimeout(() => {
        const botReplies = [
          "Good luck at the table! 🍀",
          "Let's see who takes this trick! ♠️",
          "Watch out for my trumps! 🔥",
          "Well played! 👏",
          "Kaachu Phool master in the house! 👑",
          "Ace move! Let's play! 🎴"
        ];
        const randomBot = DEFAULT_BOTS[Math.floor(Math.random() * DEFAULT_BOTS.length)];
        const replyText = botReplies[Math.floor(Math.random() * botReplies.length)];
        this.appendChatMessage({
          senderId: randomBot.id,
          senderName: randomBot.name,
          senderAvatar: randomBot.avatar,
          message: replyText,
          timestamp: Date.now()
        });
      }, 1200);
    }
  },

  appendChatMessage(msg) {
    const list = document.getElementById('webChatMessagesList');
    if (!list) return;

    const myUid = this.currentUser ? this.currentUser.uid : 'user_local';
    const isMe = msg.senderId === myUid;

    const timeStr = new Date(msg.timestamp || Date.now()).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    const safeSender = String(msg.senderName || 'Player').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    const safeText = String(msg.message || '').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    const safeAvatar = String(msg.senderAvatar || '👤').replace(/</g, '&lt;').replace(/>/g, '&gt;');

    const msgEl = document.createElement('div');
    msgEl.className = `flex gap-2 items-start ${isMe ? 'flex-row-reverse' : 'flex-row'} animate-fadeIn`;

    if (isMe) {
      msgEl.innerHTML = `
        <div class="w-7 h-7 rounded-full bg-goldPrimary/20 border border-goldPrimary/40 flex items-center justify-center text-sm shrink-0">
          ${safeAvatar}
        </div>
        <div class="max-w-[78%] flex flex-col items-end">
          <div class="flex items-center gap-1.5 mb-0.5">
            <span class="text-[9px] font-mono text-emerald-300/60">${timeStr}</span>
            <span class="text-[10px] font-bold text-goldPrimary font-mono">You</span>
          </div>
          <div class="px-3 py-2 rounded-2xl rounded-tr-sm bg-gradient-to-br from-goldPrimary to-amber-500 text-black text-xs font-semibold shadow-md break-words">
            ${safeText}
          </div>
        </div>
      `;
    } else {
      msgEl.innerHTML = `
        <div class="w-7 h-7 rounded-full bg-emerald-950 border border-emeraldBorder flex items-center justify-center text-sm shrink-0">
          ${safeAvatar}
        </div>
        <div class="max-w-[78%] flex flex-col items-start">
          <div class="flex items-center gap-1.5 mb-0.5">
            <span class="text-[10px] font-bold text-emerald-300 font-mono">${safeSender}</span>
            <span class="text-[9px] font-mono text-white/40">${timeStr}</span>
          </div>
          <div class="px-3 py-2 rounded-2xl rounded-tl-sm bg-black/80 border border-emeraldBorder/60 text-white text-xs font-medium shadow-md break-words">
            ${safeText}
          </div>
        </div>
      `;
    }

    list.appendChild(msgEl);
    list.scrollTop = list.scrollHeight;

    // If chat is closed and it's from another player, increment unread badge & sound
    if (!this.isChatOpen && !isMe) {
      this.unreadChatCount = (this.unreadChatCount || 0) + 1;
      const unreadBadge = document.getElementById('webChatUnreadBadge');
      if (unreadBadge) {
        unreadBadge.textContent = this.unreadChatCount > 9 ? '9+' : this.unreadChatCount;
        unreadBadge.classList.remove('hidden');
      }
      soundManager.playChatSound();
    }
  },

  // Fast Turn Countdown Timer with Circular SVG Progress Ring
  startTurnTimer() {
    this.stopTurnTimer();
    this.TURN_TIMEOUT_SEC = this.delays.turnTimeout;
    this.turnTimeRemaining = this.TURN_TIMEOUT_SEC;

    const curPlayer = this.players[this.currentTurnIndex];
    if (!curPlayer) return;

    const myId = (this.currentUser && this.mode === 'MULTIPLAYER') ? this.currentUser.uid : 'user_local';
    let localPlayerIndex = this.players.findIndex(p => p.id === myId);
    if (localPlayerIndex === -1) localPlayerIndex = 0;

    const numPlayers = this.players.length;
    let seatMapping = {};
    if (numPlayers === 2) {
      seatMapping.bottom = this.players[localPlayerIndex];
      seatMapping.top = this.players[(localPlayerIndex + 1) % 2];
    } else if (numPlayers === 3) {
      seatMapping.bottom = this.players[localPlayerIndex];
      seatMapping.left = this.players[(localPlayerIndex + 1) % 3];
      seatMapping.right = this.players[(localPlayerIndex + 2) % 3];
    } else {
      seatMapping.bottom = this.players[localPlayerIndex];
      seatMapping.left = this.players[(localPlayerIndex + 1) % 4];
      seatMapping.top = this.players[(localPlayerIndex + 2) % 4];
      seatMapping.right = this.players[(localPlayerIndex + 3) % 4];
    }

    let activePos = 'bottom';
    for (const [pos, p] of Object.entries(seatMapping)) {
      if (p && p.id === curPlayer.id) {
        activePos = pos;
        break;
      }
    }

    // Reset all timer rings and badges
    ['bottom', 'left', 'top', 'right'].forEach(pos => {
      const ring = document.getElementById(`webPod_${pos}_timerRing`);
      const badge = document.getElementById(`webPod_${pos}_timerBadge`);
      if (ring) {
        ring.classList.add('hidden');
        ring.style.strokeDashoffset = '0';
      }
      if (badge) badge.classList.add('hidden');
    });

    const activeRing = document.getElementById(`webPod_${activePos}_timerRing`);
    const activeBadge = document.getElementById(`webPod_${activePos}_timerBadge`);

    if (activeRing) activeRing.classList.remove('hidden');
    if (activeBadge) {
      activeBadge.classList.remove('hidden');
      activeBadge.textContent = `${this.TURN_TIMEOUT_SEC}s`;
    }

    const circumference = 113.1; // 2 * pi * 18
    let lastSecondInt = this.TURN_TIMEOUT_SEC;

    this.turnTimerInterval = setInterval(() => {
      this.turnTimeRemaining = Math.max(0, this.turnTimeRemaining - 0.1);
      const progress = (this.TURN_TIMEOUT_SEC - this.turnTimeRemaining) / this.TURN_TIMEOUT_SEC;
      const offset = circumference * progress;

      if (activeRing) {
        activeRing.style.strokeDashoffset = offset.toFixed(1);
        if (this.turnTimeRemaining > 8) {
          activeRing.style.stroke = '#d4a843'; // Gold
        } else if (this.turnTimeRemaining > 3) {
          activeRing.style.stroke = '#f97316'; // Warning Orange
        } else {
          activeRing.style.stroke = '#ef4444'; // Urgent Red
        }
      }

      const curSecInt = Math.ceil(this.turnTimeRemaining);
      if (activeBadge) {
        activeBadge.textContent = `${curSecInt}s`;
        if (this.turnTimeRemaining > 8) {
          activeBadge.style.color = '#fde047';
          activeBadge.style.borderColor = 'rgba(212,168,67,0.5)';
        } else if (this.turnTimeRemaining > 3) {
          activeBadge.style.color = '#fdba74';
          activeBadge.style.borderColor = 'rgba(249,115,22,0.6)';
        } else {
          activeBadge.style.color = '#fca5a5';
          activeBadge.style.borderColor = 'rgba(239,68,68,0.8)';
        }
      }

      // Play subtle warning tick when <= 3s
      if (curSecInt !== lastSecondInt) {
        lastSecondInt = curSecInt;
        const activePlayer = this.players[this.currentTurnIndex];
        const isMyTurn = activePlayer && (activePlayer.id === 'user_local' || (this.currentUser && activePlayer.id === this.currentUser.uid));
        if (curSecInt <= 3 && curSecInt > 0 && isMyTurn) {
          soundManager.playWarningTick();
        }
      }

      // Check if turn time elapsed -> force play
      if (this.turnTimeRemaining <= 0) {
        this.stopTurnTimer();
        this.onTurnTimeout();
      }
    }, 100);
  },

  stopTurnTimer() {
    if (this.turnTimerInterval) {
      clearInterval(this.turnTimerInterval);
      this.turnTimerInterval = null;
    }
    ['bottom', 'left', 'top', 'right'].forEach(pos => {
      const ring = document.getElementById(`webPod_${pos}_timerRing`);
      const badge = document.getElementById(`webPod_${pos}_timerBadge`);
      if (ring) ring.classList.add('hidden');
      if (badge) badge.classList.add('hidden');
    });
  },

  onTurnTimeout() {
    const activePlayer = this.players[this.currentTurnIndex];
    if (!activePlayer) return;

    if (this.mode === 'MULTIPLAYER') {
      const myUid = this.currentUser ? this.currentUser.uid : 'user_local';
      // If it's my turn or I'm the host managing timeout
      if (activePlayer.id === myUid || this.isHost) {
        if (this.isBiddingPhase) {
          const cardsCount = this.roundsSequence[this.roundIndex] || 1;
          const forcedBid = this.calculateBotBid(activePlayer.id, cardsCount);
          document.getElementById('webBiddingControls')?.classList.add('hidden');
          this.triggerFloatingEmoji('⏱️', `${activePlayer.name}: Auto-Bid ${forcedBid}`);
          this.submitMultiplayerBid(activePlayer.id, forcedBid);
        } else {
          const hand = this.dealtHands[activePlayer.id] || [];
          const legalCards = this.getLegalCards(hand);
          const cardToPlay = legalCards.length > 0 ? legalCards[0] : hand[0];
          if (cardToPlay) {
            this.triggerFloatingEmoji('⏱️', `${activePlayer.name}: Time Expired!`);
            this.submitMultiplayerPlayCard(activePlayer.id, cardToPlay);
          }
        }
      }
      return;
    }

    // Single Player Timeout Fallback
    if (this.isBiddingPhase) {
      const cardsCount = this.roundsSequence[this.roundIndex];
      const forcedBid = this.calculateBotBid(activePlayer.id, cardsCount);
      this.bids[activePlayer.id] = forcedBid;
      document.getElementById('webBiddingControls')?.classList.add('hidden');
      this.triggerFloatingEmoji('⏱️', `${activePlayer.name}: Auto-Bid ${forcedBid}`);
      soundManager.playClick();
      this.advanceBidding();
    } else {
      const hand = this.dealtHands[activePlayer.id] || [];
      const legalCards = this.getLegalCards(hand);
      const cardToPlay = legalCards.length > 0 ? legalCards[0] : hand[0];

      if (cardToPlay) {
        this.triggerFloatingEmoji('⏱️', `${activePlayer.name}: Time Expired!`);
        this.playCard(activePlayer.id, cardToPlay);
      }
    }
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

    // Setup 4 Players for offline mode
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

    // Focus table and enter fullscreen mode
    this.takeToTableAndFullscreen();

    this.startRound();
  },

  startRound() {
    const cardsCount = this.roundsSequence[this.roundIndex];
    // Trump rotation: Spades (Ka) -> Diamonds (Chu) -> Clubs (Fu) -> Hearts (L)
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

    // Start fast turn timer
    this.startTurnTimer();

    if (currentPlayer.isBot) {
      setTimeout(() => {
        const botBid = this.calculateBotBid(currentPlayer.id, cardsCount);
        this.bids[currentPlayer.id] = botBid;
        soundManager.playClick();
        this.advanceBidding();
      }, this.delays.botBid);
    } else {
      // User turn to bid -> show bid modal / chip buttons
      this.showUserBiddingControls(cardsCount);
    }
  },

  calculateBotBid(botId, cardsCount) {
    const hand = this.dealtHands[botId] || [];
    let expectedTricks = 0;

    hand.forEach(c => {
      if (c.suit === this.trumpSuit) {
        expectedTricks += c.val >= 10 ? 1 : 0.6;
      } else if (c.val >= 13) {
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

    const header = document.createElement('div');
    header.className = 'w-full text-center space-y-1';
    header.innerHTML = `
      <div class="text-[11px] font-mono text-goldPrimary uppercase tracking-widest font-bold">Predict Tricks (${this.TURN_TIMEOUT_SEC}s Pace)</div>
      <div class="text-sm sm:text-base font-display font-extrabold text-white">How many tricks will you win?</div>
      <div class="text-[10px] font-mono text-emerald-200">Round ${this.roundIndex + 1} (${maxCards} ${maxCards === 1 ? 'Card' : 'Cards'})</div>
    `;
    container.appendChild(header);

    const btnGrid = document.createElement('div');
    btnGrid.className = 'flex flex-wrap items-center justify-center gap-2 pt-2';

    for (let i = 0; i <= maxCards; i++) {
      const btn = document.createElement('button');
      btn.className = 'w-10 h-10 sm:w-12 sm:h-12 rounded-2xl bg-gradient-to-br from-goldPrimary to-amber-500 hover:from-goldLight hover:to-amber-400 text-black font-extrabold font-display text-base sm:text-lg shadow-xl transition-all transform hover:scale-110 active:scale-95 cursor-pointer border border-goldLight';
      btn.textContent = i;
      btn.onclick = () => {
        soundManager.playClick();
        this.stopTurnTimer();
        container.classList.add('hidden');

        if (this.mode === 'MULTIPLAYER') {
          const myUid = this.currentUser ? this.currentUser.uid : 'user_local';
          this.submitMultiplayerBid(myUid, i);
        } else {
          this.bids['user_local'] = i;
          this.advanceBidding();
        }
      };
      btnGrid.appendChild(btn);
    }
    container.appendChild(btnGrid);
  },

  advanceBidding() {
    this.stopTurnTimer();
    this.renderTableUI();

    // Check if all players have bid
    if (Object.keys(this.bids).length === this.players.length) {
      this.isBiddingPhase = false;
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
    this.startTurnTimer();

    if (currentPlayer.isBot) {
      setTimeout(() => {
        const playedCard = this.selectBotCard(currentPlayer.id);
        this.playCard(currentPlayer.id, playedCard);
      }, this.delays.botPlay);
    } else {
      // User turn
      this.renderTableUI();
    }
  },

  selectBotCard(botId) {
    const hand = this.dealtHands[botId] || [];
    const legalCards = this.getLegalCards(hand);

    if (legalCards.length === 0) return hand[0];

    if (!this.leadSuit) {
      return legalCards[0];
    } else {
      return legalCards[legalCards.length - 1];
    }
  },

  getLegalCards(hand) {
    if (!this.leadSuit) return hand;
    const sameSuitCards = hand.filter(c => c.suit === this.leadSuit);
    return sameSuitCards.length > 0 ? sameSuitCards : hand;
  },

  playCard(playerId, card) {
    this.stopTurnTimer();
    soundManager.playCard();

    // Remove card from hand
    const hand = this.dealtHands[playerId];
    if (hand) {
      const idx = hand.findIndex(c => c.suit === card.suit && c.rank === card.rank);
      if (idx !== -1) hand.splice(idx, 1);
    }

    // Set lead suit if first card in trick
    if (this.currentTrickCards.length === 0) {
      this.leadSuit = card.suit;
    }

    this.currentTrickCards.push({ playerId, card });
    this.renderTableUI();

    // Check if trick complete
    if (this.currentTrickCards.length === this.players.length) {
      this.isTrickFinished = true;
      setTimeout(() => this.evaluateTrickWinner(), this.delays.trickEval);
    } else {
      this.currentTurnIndex = (this.currentTurnIndex + 1) % this.players.length;
      this.renderTableUI();
      this.processPlayingTurn();
    }
  },

  evaluateTrickWinner() {
    this.stopTurnTimer();
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
    if (winner) {
      this.tricksWon[winner.id] = (this.tricksWon[winner.id] || 0) + 1;
      this.trickWinnerMessage = `👑 ${winner.name} won trick!`;
      soundManager.playTrickWin();
    }

    this.renderTableUI();

    setTimeout(() => {
      this.currentTrickCards = [];
      this.leadSuit = null;
      this.isTrickFinished = false;
      this.trickWinnerMessage = '';

      // Check if round complete
      const myId = (this.currentUser && this.mode === 'MULTIPLAYER') ? this.currentUser.uid : 'user_local';
      const userHand = this.dealtHands[myId] || [];
      if (userHand.length === 0) {
        this.finishRound();
      } else {
        if (winner) {
          this.currentTurnIndex = this.players.findIndex(p => p.id === winner.id);
        }
        this.renderTableUI();
        this.processPlayingTurn();
      }
    }, this.delays.trickClear);
  },

  finishRound() {
    this.stopTurnTimer();
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
      soundManager.playVictory();
      this.showGameOverOverlay();
    } else {
      this.dealerIndex = (this.dealerIndex + 1) % this.players.length;
      this.startRound();
    }
  },

  showGameOverOverlay() {
    this.stopTurnTimer();
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

    if (winnerText && winner) {
      winnerText.textContent = `${winner.avatar} ${winner.name} Wins the Match! (${highestScore} pts)`;
    }

    if (statsList) {
      const sorted = [...this.players].sort((a, b) => (this.scores[b.id] || 0) - (this.scores[a.id] || 0));
      const medals = ['🥇', '🥈', '🥉', '4th'];

      statsList.innerHTML = sorted.map((p, idx) => `
        <div class="flex items-center justify-between p-3 rounded-2xl ${idx === 0 ? 'bg-goldPrimary/20 border-2 border-goldPrimary' : 'bg-black/50 border border-goldPrimary/30'}">
          <div class="flex items-center gap-2">
            <span class="text-lg">${medals[idx]}</span>
            <span class="text-base">${p.avatar}</span>
            <span class="font-bold text-sm text-white">${p.name}</span>
          </div>
          <span class="font-mono ${idx === 0 ? 'text-goldPrimary' : 'text-emerald-300'} font-bold text-base">${this.scores[p.id] || 0} pts</span>
        </div>
      `).join('');
    }

    overlay.classList.remove('hidden');
  },

  renderTableUI() {
    const roundLabel = document.getElementById('webTableRoundLabel');
    const trumpLabel = document.getElementById('webTableTrumpLabel');
    const statusText = document.getElementById('webTableStatusText');
    const mobileStatusPill = document.getElementById('webMobileStatusPill');

    const cardsCount = this.roundsSequence[this.roundIndex] || 1;
    const trumpObj = SUITS.find(s => s.id === this.trumpSuit) || SUITS[0];

    if (roundLabel) roundLabel.textContent = `Round ${this.roundIndex + 1}/${this.roundsSequence.length} (${cardsCount}c)`;
    if (trumpLabel) {
      trumpLabel.innerHTML = `<span class="${trumpObj.color} text-base font-bold mr-1">${trumpObj.symbol}</span> <span class="font-bold text-white">${trumpObj.name}</span>`;
    }

    const curPlayer = this.players[this.currentTurnIndex] || this.players[0];
    const myId = (this.currentUser && this.mode === 'MULTIPLAYER') ? this.currentUser.uid : 'user_local';
    const isMyTurn = curPlayer && curPlayer.id === myId;

    let statusMsg = 'Game Ready';
    if (this.trickWinnerMessage) {
      statusMsg = this.trickWinnerMessage;
    } else if (this.isBiddingPhase) {
      statusMsg = isMyTurn ? `⭐ YOUR TURN TO BID (${this.TURN_TIMEOUT_SEC}s)` : `Bidding: ${curPlayer ? curPlayer.name : ''}'s turn (${this.TURN_TIMEOUT_SEC}s)...`;
    } else {
      statusMsg = isMyTurn ? `⭐ YOUR TURN TO PLAY A CARD (${this.TURN_TIMEOUT_SEC}s)` : `${curPlayer ? curPlayer.name : ''} is playing (${this.TURN_TIMEOUT_SEC}s)...`;
    }

    if (statusText) statusText.textContent = statusMsg;
    if (mobileStatusPill) mobileStatusPill.textContent = statusMsg;

    // Arrange player pods based on active player count (2, 3, or 4 players - ZERO BOTS)
    // Local player is always at 'bottom'
    let localPlayerIndex = this.players.findIndex(p => p.id === myId);
    if (localPlayerIndex === -1) localPlayerIndex = 0;

    const numPlayers = this.players.length;
    const podBoxes = {
      bottom: document.getElementById('webPod_bottom_box'),
      top: document.getElementById('webPod_top_box'),
      left: document.getElementById('webPod_left_box'),
      right: document.getElementById('webPod_right_box')
    };

    // Reset visibility of all pods
    Object.values(podBoxes).forEach(box => {
      if (box) box.parentElement.classList.remove('invisible', 'hidden');
    });

    let seatMapping = {}; // podPosition -> player object
    if (numPlayers === 2) {
      seatMapping.bottom = this.players[localPlayerIndex];
      seatMapping.top = this.players[(localPlayerIndex + 1) % 2];
      if (podBoxes.left) podBoxes.left.parentElement.classList.add('invisible');
      if (podBoxes.right) podBoxes.right.parentElement.classList.add('invisible');
    } else if (numPlayers === 3) {
      seatMapping.bottom = this.players[localPlayerIndex];
      seatMapping.left = this.players[(localPlayerIndex + 1) % 3];
      seatMapping.right = this.players[(localPlayerIndex + 2) % 3];
      if (podBoxes.top) podBoxes.top.parentElement.classList.add('invisible');
    } else {
      // 4 Players
      seatMapping.bottom = this.players[localPlayerIndex];
      seatMapping.left = this.players[(localPlayerIndex + 1) % 4];
      seatMapping.top = this.players[(localPlayerIndex + 2) % 4];
      seatMapping.right = this.players[(localPlayerIndex + 3) % 4];
    }

    const basePodLayouts = {
      bottom: 'px-3.5 py-1.5 sm:px-5 sm:py-2 rounded-2xl transition-all flex items-center gap-3 min-w-[150px] sm:min-w-[170px] max-w-[280px]',
      top: 'px-3 py-1.5 sm:px-4 sm:py-2 rounded-2xl transition-all flex items-center gap-2 sm:gap-3 min-w-[130px] sm:min-w-[150px] max-w-[240px]',
      left: 'px-2.5 py-1.5 sm:px-3.5 sm:py-2 rounded-2xl transition-all flex items-center gap-2 min-w-[110px] sm:min-w-[130px] max-w-[180px]',
      right: 'px-2.5 py-1.5 sm:px-3.5 sm:py-2 rounded-2xl transition-all flex items-center gap-2 min-w-[110px] sm:min-w-[130px] max-w-[180px]'
    };

    Object.entries(seatMapping).forEach(([pos, p]) => {
      if (!p) return;
      const nameEl = document.getElementById(`webPod_${pos}_name`);
      const bidEl = document.getElementById(`webPod_${pos}_bid`);
      const scoreEl = document.getElementById(`webPod_${pos}_score`);
      const podBox = podBoxes[pos];
      const avatarEl = document.getElementById(`webPod_${pos}_avatar`);

      const isCurrentActive = curPlayer && curPlayer.id === p.id;

      if (nameEl) {
        const baseName = p.id === myId ? `👑 You` : p.name;
        if (isCurrentActive) {
          nameEl.innerHTML = `<span class="flex items-center gap-1.5 truncate"><span class="truncate">${baseName}</span><span class="turn-pill-badge text-[8px] sm:text-[9px] font-mono font-extrabold uppercase bg-gradient-to-r from-amber-400 to-amber-500 text-black px-1.5 py-0.5 rounded-full shadow-md shrink-0">TURN</span></span>`;
        } else {
          nameEl.textContent = baseName;
        }
      }
      if (avatarEl) avatarEl.textContent = p.avatar || '👤';
      if (bidEl) {
        const bid = this.bids[p.id] !== undefined ? this.bids[p.id] : '?';
        const won = this.tricksWon[p.id] || 0;
        bidEl.textContent = `Bid: ${bid} • W: ${won}`;
      }
      if (scoreEl) scoreEl.textContent = `${this.scores[p.id] || 0} pts`;

      if (podBox) {
        const baseLayout = basePodLayouts[pos] || 'px-3 py-2 rounded-2xl flex items-center gap-2';
        if (isCurrentActive) {
          podBox.className = `${baseLayout} active-player-pod`;
        } else {
          podBox.className = `${baseLayout} inactive-player-pod`;
        }
      }
    });

    // Render Center Played Trick Cards
    const trickContainer = document.getElementById('webCenterTrickCards');
    if (trickContainer) {
      trickContainer.innerHTML = '';
      this.currentTrickCards.forEach(item => {
        const suitObj = SUITS.find(s => s.id === item.card.suit) || SUITS[0];
        const player = this.players.find(p => p.id === item.playerId);

        const cardWrapper = document.createElement('div');
        cardWrapper.className = 'flex flex-col items-center animate-bounce-short';

        const cardDiv = document.createElement('div');
        cardDiv.className = `w-14 sm:w-16 h-20 sm:h-24 rounded-2xl ${suitObj.bg} ${suitObj.border} border-2 p-2 flex flex-col justify-between shadow-2xl transform hover:scale-105 transition-all select-none`;
        cardDiv.innerHTML = `
          <div class="text-xs font-bold font-display ${suitObj.color}">${item.card.label}</div>
          <div class="text-center text-xl sm:text-2xl ${suitObj.color}">${item.card.symbol}</div>
          <div class="text-right text-[10px] font-mono ${suitObj.color}">${item.card.label}</div>
        `;

        const playerTag = document.createElement('div');
        playerTag.className = 'text-[9px] font-mono text-goldPrimary mt-1 bg-black/80 px-1.5 rounded-full border border-goldPrimary/30';
        playerTag.textContent = player ? `${player.avatar} ${player.name.split(' ')[0]}` : '';

        cardWrapper.appendChild(cardDiv);
        cardWrapper.appendChild(playerTag);
        trickContainer.appendChild(cardWrapper);
      });
    }

    // Lead Suit Notice in bottom hand tray
    const leadNotice = document.getElementById('webLeadSuitNotice');
    if (leadNotice) {
      if (this.leadSuit) {
        const leadObj = SUITS.find(s => s.id === this.leadSuit);
        leadNotice.innerHTML = `Lead Suit: <b class="${leadObj.color}">${leadObj.symbol} ${leadObj.name}</b>`;
      } else {
        leadNotice.textContent = 'Free Lead';
      }
    }

    // Render Local Player's Hand
    const handContainer = document.getElementById('webUserHandContainer');
    if (handContainer) {
      handContainer.innerHTML = '';
      const userHand = this.dealtHands[myId] || [];
      const isUserTurnToPlay = !this.isBiddingPhase && !this.isTrickFinished && isMyTurn;
      const legalCards = this.getLegalCards(userHand);

      userHand.forEach(card => {
        const suitObj = SUITS.find(s => s.id === card.suit) || SUITS[0];
        const isLegal = isUserTurnToPlay && legalCards.some(lc => lc.suit === card.suit && lc.rank === card.rank);

        const cardBtn = document.createElement('button');
        const cardSizeClass = userHand.length > 7 ? 'w-12 sm:w-16 h-18 sm:h-24' : 'w-14 sm:w-20 h-20 sm:h-28';

        cardBtn.className = `${cardSizeClass} rounded-2xl ${suitObj.bg} ${isLegal ? 'border-2 border-goldPrimary cursor-pointer transform hover:-translate-y-4 hover:scale-110 shadow-[0_0_15px_rgba(212,168,67,0.5)] z-10' : 'border border-neutral-700 opacity-55 cursor-not-allowed'} p-2 flex flex-col justify-between shadow-xl transition-all select-none shrink-0`;
        cardBtn.innerHTML = `
          <div class="flex justify-between items-center text-xs font-bold font-display ${suitObj.color}">
            <span>${card.label}</span>
            <span>${card.symbol}</span>
          </div>
          <div class="text-center text-xl sm:text-2xl ${suitObj.color}">${card.symbol}</div>
          <div class="text-right text-[10px] font-mono ${suitObj.color}">${card.label}</div>
        `;

        if (isLegal) {
          cardBtn.onclick = (e) => {
            e.preventDefault();
            if (cardBtn.dataset.played === 'true') return;
            cardBtn.dataset.played = 'true';
            cardBtn.style.transform = 'translateY(-20px) scale(0.92)';
            cardBtn.style.opacity = '0.5';

            if (this.mode === 'MULTIPLAYER') {
              this.submitMultiplayerPlayCard(myId, card);
            } else {
              this.playCard('user_local', card);
            }
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
            <span class="${(h.scores[p.id] || 0) >= 0 ? 'text-emerald-300 font-bold' : 'text-rose-400 font-bold'}">${h.scores[p.id] || 0}</span>
            <span class="text-white/50 text-[10px] ml-1">(${h.totals[p.id] || 0})</span>
          </td>
        `).join('')}
      </tr>
    `).join('');
  },

  // Real-Time Multiplayer Engine (ALL PLAYERS JOIN 1 ROOM, ZERO BOTS)
  async joinSingleSharedRoom() {
    if (!this.currentUser) {
      alert("🔒 Multiplayer Login is compulsory! Please sign in or create an account.");
      document.getElementById('webMultiAuthModal')?.classList.remove('hidden');
      return;
    }

    const SINGLE_ROOM_CODE = "777777";
    await this.joinOrCreateNamedRoom(SINGLE_ROOM_CODE);
  },

  async joinOrCreateNamedRoom(code) {
    if (!this.rtdb) {
      alert("Connecting to multiplayer server... Try again in 2 seconds.");
      this.setupFirebase();
      return;
    }

    this.roomCode = code;
    this.mode = 'MULTIPLAYER';

    const roomRef = this.rtdb.ref(`rooms/${code}`);
    const snapshot = await roomRef.once('value');
    const myPlayer = {
      id: this.currentUser.uid,
      uid: this.currentUser.uid,
      name: this.currentUser.displayName,
      avatar: this.currentUser.avatar || '👑',
      isBot: false
    };

    if (!snapshot.exists()) {
      // Create room as host
      this.isHost = true;
      await roomRef.set({
        roomId: code,
        hostName: myPlayer.name,
        hostUid: myPlayer.uid,
        players: [myPlayer],
        gameState: 'WAITING',
        createdAt: Date.now()
      });
      this.showLobbyUI(code, myPlayer.name, [myPlayer]);
    } else {
      // Room exists: join room
      const data = snapshot.val();
      let players = data.players || [];
      if (!players.some(p => p.uid === myPlayer.uid)) {
        players.push(myPlayer);
        await roomRef.update({ players: players });
      }
      this.isHost = (data.hostUid === myPlayer.uid);
      this.showLobbyUI(code, data.hostName, players);
    }

    this.listenMultiplayerRoom(code);
  },

  async createMultiplayerRoom() {
    if (!this.currentUser) {
      alert("🔒 Multiplayer Login is compulsory! Please sign in or create an account.");
      document.getElementById('webMultiAuthModal')?.classList.remove('hidden');
      return;
    }

    const roomCode = Math.floor(100000 + Math.random() * 900000).toString();
    await this.joinOrCreateNamedRoom(roomCode);
  },

  async joinMultiplayerRoom() {
    if (!this.currentUser) {
      alert("🔒 Multiplayer Login is compulsory! Please sign in or create an account.");
      document.getElementById('webMultiAuthModal')?.classList.remove('hidden');
      return;
    }

    const codeInput = document.getElementById('webJoinRoomCodeInput');
    const code = codeInput?.value.trim();

    if (!code || code.length !== 6) {
      alert("Please enter a valid 6-digit room code.");
      return;
    }

    await this.joinOrCreateNamedRoom(code);
  },

  leaveMultiplayerRoom() {
    if (this.chatRef) {
      this.chatRef.off();
      this.chatRef = null;
    }
    if (this.rtdb && this.roomCode && this.currentUser) {
      const roomRef = this.rtdb.ref(`rooms/${this.roomCode}`);
      roomRef.once('value').then(snap => {
        if (snap.exists()) {
          const data = snap.val();
          let players = (data.players || []).filter(p => p.uid !== this.currentUser.uid);
          if (players.length === 0) {
            roomRef.remove();
          } else {
            roomRef.update({ players });
          }
        }
      });
    }
    this.roomCode = null;
  },

  showLobbyUI(code, hostName, players) {
    document.getElementById('webSetupSection')?.classList.add('hidden');
    document.getElementById('webLobbySection')?.classList.remove('hidden');

    const codeEl = document.getElementById('webLobbyCodeText');
    const playersEl = document.getElementById('webLobbyPlayersList');
    const countEl = document.getElementById('webLobbyPlayerCount');
    const startBtn = document.getElementById('webLobbyStartBtn');

    if (codeEl) codeEl.textContent = code;
    if (countEl) countEl.textContent = `${players.length} Players in Lobby (No Bots)`;

    if (playersEl) {
      playersEl.innerHTML = players.map(p => {
        const pName = p.name || 'Player';
        const pAvatar = p.avatar || '👤';
        const isHost = (pName === hostName || p.uid === this.currentUser?.uid && this.isHost);
        return `
          <div class="flex items-center gap-3 p-3 rounded-2xl bg-black/60 border border-goldPrimary/30 font-bold text-sm">
            <span class="text-2xl">${pAvatar}</span>
            <span class="text-white">${pName}</span>
            ${isHost ? '<span class="text-xs bg-goldPrimary/20 text-goldPrimary px-2.5 py-0.5 rounded-full font-mono ml-auto border border-goldPrimary/40">HOST</span>' : '<span class="text-xs text-emerald-300 font-mono ml-auto">READY</span>'}
          </div>
        `;
      }).join('');
    }

    if (startBtn) {
      if (this.isHost) {
        startBtn.classList.remove('hidden');
        startBtn.textContent = `🚀 START MATCH (${players.length} PLAYERS • NO BOTS)`;
        startBtn.onclick = () => {
          soundManager.playClick();
          this.takeToTableAndFullscreen();
          this.launchMultiplayerMatch(players);
        };
      } else {
        startBtn.classList.add('hidden');
      }
    }
  },

  launchMultiplayerMatch(playersList) {
    if (!this.rtdb || !this.roomCode) return;

    // Filter to real human players ONLY (ZERO BOTS)
    this.players = playersList.map(p => ({
      id: p.uid || p.id,
      uid: p.uid || p.id,
      name: p.name,
      avatar: p.avatar || '👑',
      isBot: false
    }));

    this.roundsSequence = GAME_MODES.QUICK.rounds;
    this.roundIndex = 0;
    this.dealerIndex = 0;
    this.scores = {};
    this.players.forEach(p => this.scores[p.id] = 0);
    this.scoresHistory = [];

    // Deal cards for round 1
    const cardsCount = this.roundsSequence[0];
    const deck = [];
    SUITS.forEach(s => {
      RANKS.forEach(r => {
        deck.push({ suit: s.id, rank: r.id, val: r.val, symbol: s.symbol, label: r.label });
      });
    });
    for (let i = deck.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [deck[i], deck[j]] = [deck[j], deck[i]];
    }

    const dealtHands = {};
    this.players.forEach((p, idx) => {
      const hand = deck.slice(idx * cardsCount, (idx + 1) * cardsCount);
      hand.sort((a, b) => (a.suit === b.suit ? b.val - a.val : a.suit.localeCompare(b.suit)));
      dealtHands[p.id] = hand;
    });

    const trumpSuit = SUITS[0].id;
    const currentTurnIndex = (this.dealerIndex + 1) % this.players.length;

    const gamePayload = {
      players: this.players,
      roundsSequence: this.roundsSequence,
      roundIndex: 0,
      dealerIndex: 0,
      currentTurnIndex: currentTurnIndex,
      currentTurnPlayerId: this.players[currentTurnIndex].id,
      trumpSuit: trumpSuit,
      leadSuit: null,
      dealtHands: dealtHands,
      bids: {},
      tricksWon: {},
      scores: this.scores,
      currentTrickCards: [],
      isBiddingPhase: true,
      isTrickFinished: false,
      trickWinnerMessage: '',
      updatedAt: Date.now()
    };

    // Update RTDB to start game for all connected clients
    this.rtdb.ref(`rooms/${this.roomCode}`).update({
      gameState: 'PLAYING',
      game: gamePayload
    });
  },

  listenMultiplayerRoom(code) {
    if (!this.rtdb) return;

    // Listen to Room Status
    this.multiplayerRoomRef = this.rtdb.ref(`rooms/${code}`);
    this.multiplayerRoomRef.on('value', (snapshot) => {
      const data = snapshot.val();
      if (!data) return;

      if (data.gameState === 'PLAYING') {
        // Automatically focus table and enter fullscreen
        this.takeToTableAndFullscreen();

        if (data.game) {
          this.syncMultiplayerGameState(data.game);
        }
      } else if (data.players) {
        this.showLobbyUI(code, data.hostName, data.players);
      }
    });

    // Listen to Emotes in Real-Time
    this.rtdb.ref(`rooms/${code}/emotes`).on('value', (snap) => {
      const emoteData = snap.val();
      if (emoteData && emoteData.timestamp > (Date.now() - 3000)) {
        this.triggerFloatingEmoji(emoteData.emoji, emoteData.senderName);
      }
    });

    // Listen to In-Game Table Live Chat in Real-Time
    if (this.chatRef) {
      this.chatRef.off();
    }
    this.chatRef = this.rtdb.ref(`rooms/${code}/chat`);
    this.chatRef.limitToLast(50).on('child_added', (snap) => {
      const msg = snap.val();
      if (msg && msg.timestamp) {
        this.appendChatMessage(msg);
      }
    });
  },

  syncMultiplayerGameState(game) {
    this.players = game.players || [];
    this.roundsSequence = game.roundsSequence || GAME_MODES.QUICK.rounds;
    this.roundIndex = game.roundIndex || 0;
    this.dealerIndex = game.dealerIndex || 0;
    this.currentTurnIndex = game.currentTurnIndex || 0;
    this.trumpSuit = game.trumpSuit || 'SPADES';
    this.leadSuit = game.leadSuit || null;
    this.dealtHands = game.dealtHands || {};
    this.bids = game.bids || {};
    this.tricksWon = game.tricksWon || {};
    this.scores = game.scores || {};
    this.currentTrickCards = game.currentTrickCards || [];
    this.isBiddingPhase = game.isBiddingPhase !== false;
    this.isTrickFinished = game.isTrickFinished || false;
    this.trickWinnerMessage = game.trickWinnerMessage || '';

    this.renderTableUI();

    const myUid = this.currentUser ? this.currentUser.uid : 'user_local';
    const curPlayer = this.players[this.currentTurnIndex];
    const isMyTurn = curPlayer && curPlayer.id === myUid;

    // Start 30s visual turn timer for the current active player
    this.startTurnTimer();

    if (this.isBiddingPhase) {
      if (isMyTurn && this.bids[myUid] === undefined) {
        const cardsCount = this.roundsSequence[this.roundIndex] || 1;
        this.showUserBiddingControls(cardsCount);
      } else {
        document.getElementById('webBiddingControls')?.classList.add('hidden');
      }
    } else {
      document.getElementById('webBiddingControls')?.classList.add('hidden');
    }
  },

  submitMultiplayerBid(playerId, bid) {
    if (!this.rtdb || !this.roomCode) return;

    this.bids[playerId] = bid;
    const bidsRef = this.rtdb.ref(`rooms/${this.roomCode}/game/bids/${playerId}`);
    bidsRef.set(bid);

    // If all players have bid, transition to card-playing phase
    if (Object.keys(this.bids).length === this.players.length) {
      const firstTurn = (this.dealerIndex + 1) % this.players.length;
      this.rtdb.ref(`rooms/${this.roomCode}/game`).update({
        isBiddingPhase: false,
        currentTurnIndex: firstTurn,
        currentTurnPlayerId: this.players[firstTurn].id
      });
    } else {
      const nextTurn = (this.currentTurnIndex + 1) % this.players.length;
      this.rtdb.ref(`rooms/${this.roomCode}/game`).update({
        currentTurnIndex: nextTurn,
        currentTurnPlayerId: this.players[nextTurn].id
      });
    }
  },

  submitMultiplayerPlayCard(playerId, card) {
    if (!this.rtdb || !this.roomCode) return;

    soundManager.playCard();

    // Update hand
    const hand = this.dealtHands[playerId] || [];
    const idx = hand.findIndex(c => c.suit === card.suit && c.rank === card.rank);
    if (idx !== -1) hand.splice(idx, 1);

    const trickCards = [...this.currentTrickCards, { playerId, card }];
    const leadSuit = trickCards.length === 1 ? card.suit : this.leadSuit;

    // Check if trick completed
    if (trickCards.length === this.players.length) {
      // Evaluate trick winner
      let winningEntry = trickCards[0];
      for (let i = 1; i < trickCards.length; i++) {
        const candidate = trickCards[i];
        const winCard = winningEntry.card;
        const candCard = candidate.card;

        if (candCard.suit === this.trumpSuit && winCard.suit !== this.trumpSuit) {
          winningEntry = candidate;
        } else if (candCard.suit === winCard.suit && candCard.val > winCard.val) {
          winningEntry = candidate;
        }
      }

      const winner = this.players.find(p => p.id === winningEntry.playerId);
      const newTricksWon = { ...this.tricksWon, [winner.id]: (this.tricksWon[winner.id] || 0) + 1 };
      const winnerName = winner ? winner.name : 'Player';

      this.rtdb.ref(`rooms/${this.roomCode}/game`).update({
        currentTrickCards: trickCards,
        leadSuit: leadSuit,
        tricksWon: newTricksWon,
        isTrickFinished: true,
        trickWinnerMessage: `👑 ${winnerName} won trick!`,
        [`dealtHands/${playerId}`]: hand
      });

      setTimeout(() => {
        // Check if round completed (no cards remaining)
        if (hand.length === 0) {
          // Advance Round
          this.advanceMultiplayerRound(newTricksWon);
        } else {
          const nextTurnIdx = this.players.findIndex(p => p.id === winner.id);
          this.rtdb.ref(`rooms/${this.roomCode}/game`).update({
            currentTrickCards: [],
            leadSuit: null,
            isTrickFinished: false,
            trickWinnerMessage: '',
            currentTurnIndex: nextTurnIdx,
            currentTurnPlayerId: winner.id
          });
        }
      }, this.delays.multiplayerTrickClear);

    } else {
      const nextTurn = (this.currentTurnIndex + 1) % this.players.length;
      this.rtdb.ref(`rooms/${this.roomCode}/game`).update({
        currentTrickCards: trickCards,
        leadSuit: leadSuit,
        currentTurnIndex: nextTurn,
        currentTurnPlayerId: this.players[nextTurn].id,
        [`dealtHands/${playerId}`]: hand
      });
    }
  },

  advanceMultiplayerRound(finalTricksWon) {
    if (!this.rtdb || !this.roomCode) return;

    // Calculate score
    const newScores = { ...this.scores };
    this.players.forEach(p => {
      const bid = this.bids[p.id] || 0;
      const won = finalTricksWon[p.id] || 0;
      const pts = (bid === won) ? (10 + bid) : 0;
      newScores[p.id] = (newScores[p.id] || 0) + pts;
    });

    const nextRoundIndex = this.roundIndex + 1;
    if (nextRoundIndex >= this.roundsSequence.length) {
      // Game Over
      this.rtdb.ref(`rooms/${this.roomCode}/game`).update({
        scores: newScores,
        gameState: 'FINISHED'
      });
      soundManager.playVictory();
      this.showGameOverOverlay();
      return;
    }

    const nextDealer = (this.dealerIndex + 1) % this.players.length;
    const nextCardsCount = this.roundsSequence[nextRoundIndex];
    const nextTrumpSuit = SUITS[nextRoundIndex % 4].id;

    // Deal fresh cards for next round
    const deck = [];
    SUITS.forEach(s => {
      RANKS.forEach(r => {
        deck.push({ suit: s.id, rank: r.id, val: r.val, symbol: s.symbol, label: r.label });
      });
    });
    for (let i = deck.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [deck[i], deck[j]] = [deck[j], deck[i]];
    }

    const nextHands = {};
    this.players.forEach((p, idx) => {
      const h = deck.slice(idx * nextCardsCount, (idx + 1) * nextCardsCount);
      h.sort((a, b) => (a.suit === b.suit ? b.val - a.val : a.suit.localeCompare(b.suit)));
      nextHands[p.id] = h;
    });

    const nextTurn = (nextDealer + 1) % this.players.length;

    this.rtdb.ref(`rooms/${this.roomCode}/game`).update({
      roundIndex: nextRoundIndex,
      dealerIndex: nextDealer,
      currentTurnIndex: nextTurn,
      currentTurnPlayerId: this.players[nextTurn].id,
      trumpSuit: nextTrumpSuit,
      leadSuit: null,
      dealtHands: nextHands,
      bids: {},
      tricksWon: {},
      scores: newScores,
      currentTrickCards: [],
      isBiddingPhase: true,
      isTrickFinished: false,
      trickWinnerMessage: ''
    });
  }
};

// Initialize on DOM Ready
document.addEventListener('DOMContentLoaded', () => {
  window.KaachuPhoolWeb.init();
});
