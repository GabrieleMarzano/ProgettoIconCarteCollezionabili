test per verificare la Knowledge Base

% Test per archetype_compatibility
:- writeln('Test archetype_compatibility').
test_archetype_compatibility :-
    archetype_compatibility('Dark Magician', 'Elemental HERO', Compatibility1),
    writeln('Compatibilità tra Dark Magician e Elemental HERO: '), writeln(Compatibility1),

    archetype_compatibility('Blue-Eyes Dragon', 'Red-Eyes', Compatibility2),
    writeln('Compatibilità tra Blue-Eyes Dragon e Red-Eyes: '), writeln(Compatibility2),

    archetype_compatibility('Dark Magician', 'Blue-Eyes Dragon', Compatibility3),
    writeln('Compatibilità tra Dark Magician e Blue-Eyes Dragon: '), writeln(Compatibility3).

% Test per race_compatibility
:- writeln('Test race_compatibility').
test_race_compatibility :-
    race_compatibility('Dragon', 'Beast', Compatibility1),
    writeln('Compatibilità tra Dragon e Beast: '), writeln(Compatibility1),

    race_compatibility('Warrior', 'Spellcaster', Compatibility2),
    writeln('Compatibilità tra Warrior e Spellcaster: '), writeln(Compatibility2),

    race_compatibility('Dragon', 'Spellcaster', Compatibility3),
    writeln('Compatibilità tra Dragon e Spellcaster: '), writeln(Compatibility3).

% Test per attribute_compatibility
:- writeln('Test attribute_compatibility').
test_attribute_compatibility :-
    attribute_compatibility('Fire', 'Fire', Compatibility1),
    writeln('Compatibilità tra Fire e Fire: '), writeln(Compatibility1),

    attribute_compatibility('Fire', 'Water', Compatibility2),
    writeln('Compatibilità tra Fire e Water: '), writeln(Compatibility2),

    attribute_compatibility('Dark', 'Light', Compatibility3),
    writeln('Compatibilità tra Dark e Light: '), writeln(Compatibility3).

% Test per pendulum_compatibility
:- writeln('Test pendulum_compatibility').
test_pendulum_compatibility :-
    pendulum_compatibility('Pendulum Magician', 'Pendulum Dragon', Compatibility1),
    writeln('Compatibilità tra Pendulum Magician e Pendulum Dragon: '), writeln(Compatibility1),

    pendulum_compatibility('Pendulum Magician', 'Blue-Eyes White Dragon', Compatibility2),
    writeln('Compatibilità tra Pendulum Magician e Blue-Eyes White Dragon: '), writeln(Compatibility2),

    pendulum_compatibility('Pendulum Magician', 'Odd-Eyes Pendulum Dragon', Compatibility3),
    writeln('Compatibilità tra Pendulum Magician e Odd-Eyes Pendulum Dragon: '), writeln(Compatibility3).

% Test per effect_compatibility
:- writeln('Test effect_compatibility').
test_effect_compatibility :-
    effect_compatibility('Dark Magician', 'Dark Magic Attack', Compatibility1),
    writeln('Compatibilità effetti tra Dark Magician e Dark Magic Attack: '), writeln(Compatibility1),

    effect_compatibility('Blue-Eyes White Dragon', 'Blue-Eyes Shining Dragon', Compatibility2),
    writeln('Compatibilità effetti tra Blue-Eyes White Dragon e Blue-Eyes Shining Dragon: '), writeln(Compatibility2),

    effect_compatibility('Dark Magician', 'Mirror Force', Compatibility3),
    writeln('Compatibilità effetti tra Dark Magician e Mirror Force: '), writeln(Compatibility3).

% Test per card_compatibility
:- writeln('Test card_compatibility').
test_card_compatibility :-
    card_compatibility('Dark Magician', 'Blue-Eyes White Dragon', Compatibility1),
    writeln('Compatibilità totale tra Dark Magician e Blue-Eyes White Dragon: '), writeln(Compatibility1),

    card_compatibility('Dark Magician', 'Dark Magic Attack', Compatibility2),
    writeln('Compatibilità totale tra Dark Magician e Dark Magic Attack: '), writeln(Compatibility2),

    card_compatibility('Dark Magician', 'Mirror Force', Compatibility3),
    writeln('Compatibilità totale tra Dark Magician e Mirror Force: '), writeln(Compatibility3).

% Test per deck_common e cardset_common
:- writeln('Test deck_common e cardset_common').
test_common :-
    deck_common('Dark Magician', 'Dark Magic Attack', CommonDeck),
    writeln('Carte comuni nello stesso deck: '), writeln(CommonDeck),

    cardset_common('Dark Magician', 'Dark Magic Attack', CommonCardset),
    writeln('Carte comuni nello stesso cardset: '), writeln(CommonCardset),

    deck_common('Blue-Eyes White Dragon', 'Blue-Eyes Shining Dragon', CommonDeck2),
    writeln('Carte comuni nello stesso deck: '), writeln(CommonDeck2).

% Esecuzione automatica di tutti i test
:- writeln('Esecuzione di tutti i test:').
:- test_archetype_compatibility.
:- test_race_compatibility.
:- test_attribute_compatibility.
:- test_pendulum_compatibility.
:- test_effect_compatibility.
:- test_card_compatibility.
:- test_common.
