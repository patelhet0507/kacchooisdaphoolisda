import 'dart:math';
import 'package:flutter/material.dart';

void main() => runApp(const KaachuPhoolApp());

class KaachuPhoolApp extends StatelessWidget {
  const KaachuPhoolApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Kaachu Phool',
      debugShowCheckedModeBanner: false,
      theme: ThemeData.dark().copyWith(
        scaffoldBackgroundColor: const Color(0xFF052e16),
        colorScheme: const ColorScheme.dark(
          primary: Color(0xFF166534),
          secondary: Color(0xFFd4a843),
          surface: Color(0xFF0c1a12),
        ),
      ),
      home: const LandingPage(),
    );
  }
}

class LandingPage extends StatelessWidget {
  const LandingPage({super.key});

  @override
  Widget build(BuildContext context) {
    final w = MediaQuery.of(context).size.width;
    final isMobile = w < 800;

    return Scaffold(
      body: SingleChildScrollView(
        child: Column(
          children: [
            _NavBar(isMobile: isMobile),
            _HeroSection(isMobile: isMobile),
            _SuitsSection(isMobile: isMobile),
            _FeaturesSection(isMobile: isMobile),
            _HowToPlaySection(isMobile: isMobile),
            _DownloadSection(isMobile: isMobile),
            _ReleasesSection(isMobile: isMobile),
            _Footer(isMobile: isMobile),
          ],
        ),
      ),
    );
  }
}

// ─── NAV ────────────────────────────────────────────────────────────
class _NavBar extends StatelessWidget {
  final bool isMobile;
  const _NavBar({required this.isMobile});

  @override
  Widget build(BuildContext context) {
    return Positioned(
      top: 16,
      left: 0,
      right: 0,
      child: Center(
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 10),
          decoration: BoxDecoration(
            color: const Color(0xFF052e16).withValues(alpha: 0.85),
            borderRadius: BorderRadius.circular(999),
            border: Border.all(color: Colors.white.withValues(alpha: 0.06)),
          ),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Text('Kaachu Phool',
                  style: TextStyle(fontWeight: FontWeight.w700, fontSize: 14)),
              const SizedBox(width: 24),
              _GoldButton(label: 'Download APK', onTap: () {}),
            ],
          ),
        ),
      ),
    );
  }
}

// ─── HERO ───────────────────────────────────────────────────────────
class _HeroSection extends StatefulWidget {
  final bool isMobile;
  const _HeroSection({required this.isMobile});

  @override
  State<_HeroSection> createState() => _HeroSectionState();
}

class _HeroSectionState extends State<_HeroSection>
    with TickerProviderStateMixin {
  late AnimationController _shuffleCtrl;
  late AnimationController _dealCtrl;
  late AnimationController _idleCtrl;
  int _phase = 0; // 0=shuffle, 1=deal, 2=idle

  @override
  void initState() {
    super.initState();
    _shuffleCtrl = AnimationController(
        vsync: this, duration: const Duration(milliseconds: 1200));
    _dealCtrl = AnimationController(
        vsync: this, duration: const Duration(milliseconds: 1600));
    _idleCtrl = AnimationController(
        vsync: this, duration: const Duration(milliseconds: 3000));

    _shuffleCtrl.addStatusListener((s) {
      if (s == AnimationStatus.completed) {
        setState(() => _phase = 1);
        _dealCtrl.forward();
      }
    });
    _dealCtrl.addStatusListener((s) {
      if (s == AnimationStatus.completed) {
        setState(() => _phase = 2);
        _idleCtrl.forward();
      }
    });
    _idleCtrl.addStatusListener((s) {
      if (s == AnimationStatus.completed) {
        setState(() => _phase = 0);
        _shuffleCtrl.reset();
        _dealCtrl.reset();
        _idleCtrl.reset();
        _shuffleCtrl.forward();
      }
    });

    Future.delayed(const Duration(milliseconds: 500), () {
      _shuffleCtrl.forward();
    });
  }

  @override
  void dispose() {
    _shuffleCtrl.dispose();
    _dealCtrl.dispose();
    _idleCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      constraints: const BoxConstraints(minHeight: 700),
      padding: EdgeInsets.symmetric(
          horizontal: widget.isMobile ? 24 : 64, vertical: 100),
      decoration: const BoxDecoration(
        gradient: RadialGradient(
          center: Alignment(-0.6, -0.3),
          radius: 1.2,
          colors: [Color(0x4D166534), Colors.transparent],
        ),
      ),
      child: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 1100),
          child: widget.isMobile
              ? Column(
                  children: [
                    _HeroCopy(),
                    const SizedBox(height: 48),
                    SizedBox(
                        height: 360,
                        child: _CardShuffle(
                          shuffleAnim: _shuffleCtrl,
                          dealAnim: _dealCtrl,
                          idleAnim: _idleCtrl,
                          phase: _phase,
                        )),
                  ],
                )
              : Row(
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: [
                    Expanded(child: _HeroCopy()),
                    const SizedBox(width: 64),
                    SizedBox(
                      width: 460,
                      height: 400,
                      child: _CardShuffle(
                        shuffleAnim: _shuffleCtrl,
                        dealAnim: _dealCtrl,
                        idleAnim: _idleCtrl,
                        phase: _phase,
                      ),
                    ),
                  ],
                ),
        ),
      ),
    );
  }
}

class _HeroCopy extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        _Tag(label: 'FREE ANDROID GAME'),
        const SizedBox(height: 24),
        Text('Kaachu\nPhool',
            style: const TextStyle(
                fontSize: 72,
                fontWeight: FontWeight.w700,
                height: 1.05,
                letterSpacing: -2)),
        const SizedBox(height: 8),
        ShaderMask(
          shaderCallback: (r) => const LinearGradient(colors: [
            Color(0xFFd4a843),
            Color(0xFFb8960b),
          ]).createShader(r),
          child: const Text('Kaachu\nPhool',
              style: TextStyle(
                  fontSize: 72,
                  fontWeight: FontWeight.w700,
                  height: 1.05,
                  letterSpacing: -2,
                  color: Colors.white)),
        ),
        const SizedBox(height: 20),
        Text(
          'The classic Indian trick-taking card game.\nBid, bluff, and outsmart your opponents.',
          style: TextStyle(
              color: Colors.white.withValues(alpha: 0.45),
              fontSize: 17,
              height: 1.6),
        ),
        const SizedBox(height: 32),
        _GoldButton(
          label: 'Download Free',
          onTap: () {},
          icon: Icons.download,
          large: true,
        ),
        const SizedBox(height: 16),
        Text('Android 7.0+  ·  ~20 MB  ·  No ads',
            style: TextStyle(
                color: Colors.white.withValues(alpha: 0.3),
                fontSize: 13,
                fontFamily: 'monospace')),
      ],
    );
  }
}

// ─── CARD SHUFFLE ANIMATION ─────────────────────────────────────────
class _CardShuffle extends StatelessWidget {
  final AnimationController shuffleAnim;
  final AnimationController dealAnim;
  final AnimationController idleAnim;
  final int phase;

  const _CardShuffle({
    required this.shuffleAnim,
    required this.dealAnim,
    required this.idleAnim,
    required this.phase,
  });

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: Listenable.merge([shuffleAnim, dealAnim, idleAnim]),
      builder: (context, _) {
        return Stack(
          alignment: Alignment.center,
          children: List.generate(4, (i) => _buildCard(i)),
        );
      },
    );
  }

  Widget _buildCard(int index) {
    final suits = [
      _CardData('Spades', 'Kali', const Color(0xFF166534), Icons.spades),
      _CardData('Diamonds', 'Chokat', const Color(0xFF1a5c3a), Icons.diamond),
      _CardData('Clubs', 'Fuli', const Color(0xFF166534), Icons.clubs),
      _CardData('Hearts', 'Laal', const Color(0xFF7f1d1d), Icons.favorite),
    ];
    final data = suits[index];

    double x = 0, y = 0, rot = 0, scale = 1, opacity = 1;

    if (phase == 0) {
      // Shuffle: cards stack on top of each other with slight flutter
      final progress = shuffleAnim.value;
      final flutter = sin(progress * pi * 6 + index * 0.8) * 8;
      x = flutter;
      y = -index * 2.0;
      rot = flutter * 0.005;
      scale = 1.0 - index * 0.02;
      opacity = 1.0;
    } else if (phase == 1) {
      // Deal: cards fan out to final positions
      final progress = Curves.easeOutCubic.transform(dealAnim.value);
      final targetX = (index - 1.5) * 100.0;
      final targetY = (index % 2 == 0 ? -20.0 : 20.0);
      final targetRot = (index - 1.5) * 0.08;
      x = Curves.easeOutCubic.transform(progress) * targetX;
      y = Curves.easeOutCubic.transform(progress) * targetY;
      rot = Curves.easeOutCubic.transform(progress) * targetRot;
      scale = 0.85 + Curves.easeOutCubic.transform(progress) * 0.15;
      opacity = Curves.easeIn.transform(progress);
    } else {
      // Idle: gentle float
      final t = idleAnim.value;
      final targetX = (index - 1.5) * 100.0;
      final targetY = (index % 2 == 0 ? -20.0 : 20.0);
      final targetRot = (index - 1.5) * 0.08;
      x = targetX + sin(t * pi * 2 + index) * 4;
      y = targetY + cos(t * pi * 2 + index * 0.7) * 6;
      rot = targetRot + sin(t * pi * 2 + index) * 0.01;
      scale = 1.0;
      opacity = 1.0;
    }

    return Transform(
      alignment: Alignment.center,
      transform: Matrix4.identity()
        ..translate(x, y)
        ..rotateZ(rot)
        ..scale(scale),
      child: Opacity(
        opacity: opacity,
        child: Container(
          width: 180,
          height: 260,
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(18),
            gradient: LinearGradient(
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
              colors: [data.color, data.color.withValues(alpha: 0.7)],
            ),
            border: Border.all(color: Colors.white.withValues(alpha: 0.08)),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withValues(alpha: 0.4),
                blurRadius: 30,
                offset: const Offset(0, 15),
              ),
            ],
          ),
          child: Stack(
            children: [
              // Inner border
              Positioned.fill(
                margin: const EdgeInsets.all(8),
                child: Container(
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(12),
                    border:
                        Border.all(color: Colors.white.withValues(alpha: 0.06)),
                  ),
                ),
              ),
              // Content
              Center(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Icon(data.icon, size: 44, color: Colors.white70),
                    const SizedBox(height: 12),
                    Text(data.name,
                        style: const TextStyle(
                            fontSize: 20,
                            fontWeight: FontWeight.w700,
                            color: Colors.white)),
                    const SizedBox(height: 4),
                    Text(data.gujarati,
                        style: TextStyle(
                            fontSize: 11,
                            fontFamily: 'monospace',
                            letterSpacing: 1.5,
                            color: Colors.white.withValues(alpha: 0.4))),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _CardData {
  final String name;
  final String gujarati;
  final Color color;
  final IconData icon;
  const _CardData(this.name, this.gujarati, this.color, this.icon);
}

// ─── SUITS SECTION ──────────────────────────────────────────────────
class _SuitsSection extends StatelessWidget {
  final bool isMobile;
  const _SuitsSection({required this.isMobile});

  @override
  Widget build(BuildContext context) {
    return _Section(
      isMobile: isMobile,
      tag: 'THE GAME',
      title: 'K-A-C-H-U-F-U-L',
      desc: 'A Gujarati mnemonic for the strict cyclical rotation of trump suits.',
      child: Wrap(
        spacing: 16,
        runSpacing: 16,
        alignment: WrapAlignment.center,
        children: const [
          _SuitTile('Ka', 'Kali — Spades', Icons.spades, Color(0xFFFFFFFF)),
          _SuitTile('Chu', 'Chokat — Diamonds', Icons.diamond, Color(0xFFd4a843)),
          _SuitTile('Fu', 'Fuli — Clubs', Icons.clubs, Color(0xFFFFFFFF)),
          _SuitTile('L', 'Laal — Hearts', Icons.favorite, Color(0xFFfca5a5)),
        ],
      ),
    );
  }
}

class _SuitTile extends StatelessWidget {
  final String letter, label;
  final IconData icon;
  final Color iconColor;
  const _SuitTile(this.letter, this.label, this.icon, this.iconColor);

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 140,
      padding: const EdgeInsets.symmetric(vertical: 24),
      decoration: BoxDecoration(
        color: Colors.white.withValues(alpha: 0.02),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.white.withValues(alpha: 0.06)),
      ),
      child: Column(
        children: [
          Icon(icon, size: 32, color: iconColor),
          const SizedBox(height: 10),
          Text(letter,
              style: const TextStyle(
                  fontSize: 20, fontWeight: FontWeight.w700)),
          const SizedBox(height: 2),
          Text(label,
              style: TextStyle(
                  fontSize: 11,
                  fontFamily: 'monospace',
                  color: Colors.white.withValues(alpha: 0.4))),
        ],
      ),
    );
  }
}

// ─── FEATURES ───────────────────────────────────────────────────────
class _FeaturesSection extends StatelessWidget {
  final bool isMobile;
  const _FeaturesSection({required this.isMobile});

  @override
  Widget build(BuildContext context) {
    final features = [
      _Feat('AI Bots', '5 characters, 3 difficulty levels.', Icons.psychology),
      _Feat('Multiplayer', 'Room codes, chat, voice notes.', Icons.language),
      _Feat('7 Game Modes', 'Quick Match to Grand Ladder.', Icons.grid_view),
      _Feat('3 Scoring Rules', 'Standard, Penalty, Bonus.', Icons.bar_chart),
      _Feat('Achievements', '8 achievements, 6 avatars.', Icons.emoji_events),
      _Feat('Scorekeeper', 'Persistent match history.', Icons.scoreboard),
    ];

    return _Section(
      isMobile: isMobile,
      tag: 'FEATURES',
      title: 'Everything You Need',
      child: Wrap(
        spacing: 16,
        runSpacing: 16,
        alignment: WrapAlignment.center,
        children: features
            .map((f) => Container(
                  width: isMobile ? double.infinity : 280,
                  padding: const EdgeInsets.all(24),
                  decoration: BoxDecoration(
                    color: Colors.white.withValues(alpha: 0.02),
                    borderRadius: BorderRadius.circular(16),
                    border:
                        Border.all(color: Colors.white.withValues(alpha: 0.06)),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Container(
                        width: 40,
                        height: 40,
                        decoration: BoxDecoration(
                          color: const Color(0xFF166534).withValues(alpha: 0.4),
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: Icon(f.icon, size: 20, color: const Color(0xFFd4a843)),
                      ),
                      const SizedBox(height: 14),
                      Text(f.title,
                          style: const TextStyle(
                              fontSize: 16, fontWeight: FontWeight.w600)),
                      const SizedBox(height: 6),
                      Text(f.desc,
                          style: TextStyle(
                              fontSize: 13,
                              color: Colors.white.withValues(alpha: 0.4),
                              height: 1.5)),
                    ],
                  ),
                ))
            .toList(),
      ),
    );
  }
}

class _Feat {
  final String title, desc;
  final IconData icon;
  const _Feat(this.title, this.desc, this.icon);
}

// ─── HOW TO PLAY ────────────────────────────────────────────────────
class _HowToPlaySection extends StatelessWidget {
  final bool isMobile;
  const _HowToPlaySection({required this.isMobile});

  @override
  Widget build(BuildContext context) {
    final steps = [
      _Step('1', 'Bid Your Hand', 'Predict your tricks. The Hook Rule keeps the dealer honest.'),
      _Step('2', 'Play Your Cards', 'Follow suit, play trumps. Trump shifts each round.'),
      _Step('3', 'Score & Repeat', 'Hit your bid for bonus points. Highest score wins.'),
    ];

    return _Section(
      isMobile: isMobile,
      tag: 'HOW TO PLAY',
      title: 'Simple Rules, Deep Strategy',
      child: isMobile
          ? Column(children: steps.map((s) => _buildStep(s)).toList())
          : Row(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: steps.map((s) => Expanded(child: _buildStep(s))).toList(),
            ),
    );
  }

  Widget _buildStep(_Step s) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: Column(
        children: [
          Container(
            width: 48,
            height: 48,
            decoration: BoxDecoration(
              color: Colors.white.withValues(alpha: 0.03),
              borderRadius: BorderRadius.circular(14),
              border: Border.all(color: Colors.white.withValues(alpha: 0.06)),
            ),
            child: Center(
              child: Text(s.num,
                  style: const TextStyle(
                      fontSize: 20,
                      fontWeight: FontWeight.w700,
                      color: Color(0xFFd4a843))),
            ),
          ),
          const SizedBox(height: 16),
          Text(s.title,
              style:
                  const TextStyle(fontSize: 16, fontWeight: FontWeight.w600)),
          const SizedBox(height: 6),
          Text(s.desc,
              textAlign: TextAlign.center,
              style: TextStyle(
                  fontSize: 13,
                  color: Colors.white.withValues(alpha: 0.4),
                  height: 1.5)),
        ],
      ),
    );
  }
}

class _Step {
  final String num, title, desc;
  const _Step(this.num, this.title, this.desc);
}

// ─── DOWNLOAD ───────────────────────────────────────────────────────
class _DownloadSection extends StatelessWidget {
  final bool isMobile;
  const _DownloadSection({required this.isMobile});

  @override
  Widget build(BuildContext context) {
    return _Section(
      isMobile: isMobile,
      tag: 'DOWNLOAD',
      title: 'Get Kaachu Phool',
      desc: 'Free to play. No ads. No account required.',
      child: Column(
        children: [
          _GoldButton(
            label: 'Download Latest APK',
            onTap: () {},
            icon: Icons.download,
            large: true,
          ),
          const SizedBox(height: 24),
          Wrap(
            spacing: 24,
            runSpacing: 8,
            alignment: WrapAlignment.center,
            children: [
              _Stat(Icons.check, 'v1.1.1.24 — Latest'),
              _Stat(Icons.check, '~20 MB'),
              _Stat(Icons.check, 'Android 7.0+'),
            ],
          ),
        ],
      ),
    );
  }
}

class _Stat extends StatelessWidget {
  final IconData icon;
  final String text;
  const _Stat(this.icon, this.text);

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Icon(icon, size: 16, color: const Color(0xFFd4a843)),
        const SizedBox(width: 6),
        Text(text,
            style: TextStyle(
                fontSize: 13, color: Colors.white.withValues(alpha: 0.35))),
      ],
    );
  }
}

// ─── RELEASES ───────────────────────────────────────────────────────
class _ReleasesSection extends StatelessWidget {
  final bool isMobile;
  const _ReleasesSection({required this.isMobile});

  @override
  Widget build(BuildContext context) {
    final releases = [
      ('v1.1.1.24', 'Sep 15, 2026', true),
      ('v1.1.1.23', 'Sep 15, 2026', false),
      ('v1.1.1.20', 'Sep 15, 2026', false),
    ];

    return _Section(
      isMobile: isMobile,
      tag: 'RELEASES',
      title: 'Version History',
      child: Column(
        children: [
          ...releases.map((r) => Container(
                margin: const EdgeInsets.only(bottom: 10),
                padding:
                    const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
                decoration: BoxDecoration(
                  color: Colors.white.withValues(alpha: 0.02),
                  borderRadius: BorderRadius.circular(14),
                  border:
                      Border.all(color: Colors.white.withValues(alpha: 0.06)),
                ),
                child: Row(
                  children: [
                    if (r.$3)
                      Container(
                        margin: const EdgeInsets.only(right: 12),
                        padding: const EdgeInsets.symmetric(
                            horizontal: 10, vertical: 4),
                        decoration: BoxDecoration(
                          color: const Color(0xFFd4a843).withValues(alpha: 0.1),
                          borderRadius: BorderRadius.circular(6),
                        ),
                        child: const Text('LATEST',
                            style: TextStyle(
                                fontSize: 11,
                                fontFamily: 'monospace',
                                fontWeight: FontWeight.w600,
                                color: Color(0xFFd4a843))),
                      ),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(r.$1,
                              style: const TextStyle(
                                  fontSize: 15, fontWeight: FontWeight.w600)),
                          Text(r.$2,
                              style: TextStyle(
                                  fontSize: 12,
                                  color: Colors.white.withValues(alpha: 0.3))),
                        ],
                      ),
                    ),
                    Icon(Icons.download,
                        size: 20,
                        color: Colors.white.withValues(alpha: 0.2)),
                  ],
                ),
              )),
          Container(
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(14),
              border: Border.all(
                  color: const Color(0xFFd4a843).withValues(alpha: 0.15),
                  style: BorderStyle.solid),
            ),
            child: Center(
              child: Text('View all releases on GitHub',
                  style: TextStyle(
                      fontSize: 13,
                      color: Colors.white.withValues(alpha: 0.3))),
            ),
          ),
        ],
      ),
    );
  }
}

// ─── FOOTER ─────────────────────────────────────────────────────────
class _Footer extends StatelessWidget {
  final bool isMobile;
  const _Footer({required this.isMobile});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 32),
      decoration: BoxDecoration(
        border: Border(
            top: BorderSide(color: Colors.white.withValues(alpha: 0.06))),
      ),
      child: isMobile
          ? Column(
              children: [
                const Text('Kaachu Phool',
                    style:
                        TextStyle(fontWeight: FontWeight.w600, fontSize: 14)),
                const SizedBox(height: 12),
                Text('GitHub  ·  Releases  ·  Issues',
                    style: TextStyle(
                        fontSize: 13,
                        color: Colors.white.withValues(alpha: 0.3))),
              ],
            )
          : Row(
              children: [
                const Text('Kaachu Phool',
                    style:
                        TextStyle(fontWeight: FontWeight.w600, fontSize: 14)),
                const Spacer(),
                Text('GitHub  ·  Releases  ·  Issues',
                    style: TextStyle(
                        fontSize: 13,
                        color: Colors.white.withValues(alpha: 0.3))),
              ],
            ),
    );
  }
}

// ─── SHARED WIDGETS ─────────────────────────────────────────────────
class _Tag extends StatelessWidget {
  final String label;
  const _Tag({required this.label});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 5),
      decoration: BoxDecoration(
        color: const Color(0xFFd4a843).withValues(alpha: 0.05),
        borderRadius: BorderRadius.circular(999),
        border:
            Border.all(color: const Color(0xFFd4a843).withValues(alpha: 0.25)),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
              width: 6,
              height: 6,
              decoration: const BoxDecoration(
                  shape: BoxShape.circle, color: Color(0xFFd4a843))),
          const SizedBox(width: 8),
          Text(label,
              style: const TextStyle(
                  fontSize: 10,
                  fontFamily: 'monospace',
                  letterSpacing: 2,
                  color: Color(0xFFd4a843))),
        ],
      ),
    );
  }
}

class _GoldButton extends StatelessWidget {
  final String label;
  final VoidCallback onTap;
  final IconData? icon;
  final bool large;

  const _GoldButton(
      {required this.label,
      required this.onTap,
      this.icon,
      this.large = false});

  @override
  Widget build(BuildContext context) {
    return Material(
      color: const Color(0xFF166534),
      borderRadius: BorderRadius.circular(999),
      child: InkWell(
        borderRadius: BorderRadius.circular(999),
        onTap: onTap,
        child: Padding(
          padding: EdgeInsets.symmetric(
              horizontal: large ? 32 : 20, vertical: large ? 16 : 10),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              if (icon != null) ...[
                Icon(icon, size: large ? 20 : 16, color: Colors.white),
                const SizedBox(width: 8),
              ],
              Text(label,
                  style: TextStyle(
                      fontSize: large ? 16 : 13,
                      fontWeight: FontWeight.w600,
                      color: Colors.white)),
            ],
          ),
        ),
      ),
    );
  }
}

class _Section extends StatelessWidget {
  final bool isMobile;
  final String tag, title;
  final String? desc;
  final Widget child;

  const _Section({
    required this.isMobile,
    required this.tag,
    required this.title,
    this.desc,
    required this.child,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: EdgeInsets.symmetric(
          horizontal: isMobile ? 24 : 64, vertical: 80),
      decoration: BoxDecoration(
        border: Border(
            top: BorderSide(color: Colors.white.withValues(alpha: 0.06))),
      ),
      child: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 960),
          child: Column(
            children: [
              _Tag(label: tag),
              const SizedBox(height: 20),
              Text(title,
                  textAlign: TextAlign.center,
                  style: const TextStyle(
                      fontSize: 38, fontWeight: FontWeight.w700, letterSpacing: -1)),
              if (desc != null) ...[
                const SizedBox(height: 16),
                Text(desc!,
                    textAlign: TextAlign.center,
                    style: TextStyle(
                        fontSize: 16,
                        color: Colors.white.withValues(alpha: 0.4),
                        height: 1.6)),
              ],
              const SizedBox(height: 48),
              child,
            ],
          ),
        ),
      ),
    );
  }
}
