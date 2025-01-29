% pesi delle metriche 
peso(effect, 0.18).
peso(archetype, 0.24).
peso(deck_comuni, 0.16).
peso(race, 0.13).
peso(attribute, 0.13).
peso(cardset_comuni, 0.12).
peso(pendulum, 0.04).

%Compatibilità per archetipi 
archetype_compatibility(Archetype1, Archetype2, 1.0) :-
    nonvar(Archetype1),
    nonvar(Archetype2),
    Archetype1 = Archetype2.

archetype_compatibility(Archetype1, Archetype2, 0.5) :-
    nonvar(Archetype1),
    nonvar(Archetype2),
    archetype_related(Archetype1, Archetype2).



%Compatibilità per razze 
race_compatibility(Race1, Race2, 1.0) :-
    nonvar(Race1),
    nonvar(Race2),
    Race1 = Race2.

race_compatibility(Race1, Race2, 0.15) :-
    nonvar(Race1),
    nonvar(Race2),
    race_related(Race1, Race2).

% Relazioni tra razze
race_related('Dragon', 'Beast').
race_related('Warrior', 'Spellcaster').

%Compatibilità per attributi 
attribute_compatibility(Attribute1, Attribute2, 1.0) :-
    nonvar(Attribute1),
    nonvar(Attribute2),
    Attribute1 = Attribute2.

attribute_compatibility(_, _, 0.0). % Nessuna compatibilità altrimenti.

%Compatibilità per Pendulum 
pendulum_compatibility(HumanType1, HumanType2, 1.0) :-
    nonvar(HumanType1),
    nonvar(HumanType2),
    sub_string(HumanType1, _, _, _, "pendulum"),
    sub_string(HumanType2, _, _, _, "pendulum").

pendulum_compatibility(_, _, 0.0). % Compatibilità nulla per le altre carte.

%Compatibilità per effetti 
effect_compatibility(Card1, Card2, 1.0) :- mentions_name(Card1, Card2).
effect_compatibility(Card1, Card2, 0.5) :- mentions_archetype(Card1, Card2).
effect_compatibility(_, _, 0.0). % Nessuna compatibilità di default.


%Compatibilità complessiva 
card_compatibility(Card1, Card2, Compatibility) :-
    % Calcola i punteggi parziali
    deck_common(Card1, Card2, DeckComuni),
    cardset_common(Card1, Card2, CardsetComuni),
    archetype_compatibility(Card1, Card2, ArchetypeCompat),
    race_compatibility(Card1, Card2, RaceCompat),
    attribute_compatibility(Card1, Card2, AttributeCompat),
    pendulum_compatibility(Card1, Card2, PendulumCompat),
    effect_compatibility(Card1, Card2, EffectCompat),

    % Ottieni i pesi
    peso(deck_comuni, PesoDeck),
    peso(cardset_comuni, PesoCardset),
    peso(archetype, PesoArchetype),
    peso(race, PesoRace),
    peso(attribute, PesoAttribute),
    peso(pendulum, PesoPendulum),
    peso(effect, PesoEffect),

    % Calcola la compatibilità totale
    Compatibility is (DeckComuni * PesoDeck +
                      CardsetComuni * PesoCardset +
                      ArchetypeCompat * PesoArchetype +
                      RaceCompat * PesoRace +
                      AttributeCompat * PesoAttribute +
                      PendulumCompat * PesoPendulum +
                      EffectCompat * PesoEffect) / 
                      (PesoDeck + PesoCardset + PesoArchetype + PesoRace + PesoAttribute + PesoPendulum + PesoEffect).

% Regola per determinare la compatibilità basata sullo stesso deck
deck_compatibility(Card1, Card2, Compatibility) :-
    deck_common(Card1, Card2, Compatibility).

% Regola per determinare la compatibilità basata sullo stesso cardset
cardset_compatibility(Card1, Card2, Compatibility) :-
    cardset_common(Card1, Card2, Compatibility).

% Query di esempio 
% Calcola la compatibilità tra 'Dark Magician' e 'Dark Magic Attack'
% ?- card_compatibility('Dark Magician', 'Dark Magic Attack', Compatibility).
% Compatibility = 0.87.


% Relazioni tra archetipi compatibili 
archetype_related('Blue-Eyes Dragon', 'Archfiend').
archetype_related('C"', 'Artifact').
archetype_related('Alligator', 'Jester').
archetype_related('Blue-Eyes Dragon', 'Barbaros').
archetype_related('Blue-Eyes Dragon', 'Book of').
archetype_related('Blue-Eyes Dragon', 'Chaos').
archetype_related('Blue-Eyes Dragon', 'Clear Wing').
archetype_related('Blue-Eyes Dragon', 'Cyber Dragon').
archetype_related('Blue-Eyes Dragon', 'Dark Magician').
archetype_related('Blue-Eyes Dragon', 'Forbidden').
archetype_related('Blue-Eyes Dragon', 'Greed').
archetype_related('Blue-Eyes Dragon', 'Legendary Knight').
archetype_related('Blue-Eyes Dragon', 'Lightsworn').
archetype_related('Blue-Eyes Dragon', 'Machina').
archetype_related('Blue-Eyes Dragon', 'Magician').
archetype_related('Blue-Eyes Dragon', 'Odd-Eyes').
archetype_related('Blue-Eyes Dragon', 'Performapal').
archetype_related('Blue-Eyes Dragon', 'Red-Eyes').
archetype_related('Blue-Eyes Dragon', 'Supreme King').
archetype_related('Blue-Eyes Dragon', 'Traptrix').
archetype_related('Blue-Eyes Dragon', 'Venom').
archetype_related('Blue-Eyes Dragon', 'X-Saber').
archetype_related('C"', 'Ancient Gear').
archetype_related('C"', 'Artifact').
archetype_related('C"', 'Crystal').
archetype_related('C"', 'Crystal Beast').
archetype_related('C"', 'Fleur').
archetype_related('C"', 'Geargia').
archetype_related('C"', 'Greed').
archetype_related('C"', 'Harpie').
archetype_related('C"', 'Hole').
archetype_related('C"', 'Kaiju').
archetype_related('C"', 'Mekk-Knight').
archetype_related('C"', 'Naturia').
archetype_related('C"', 'Phantom Knights').
archetype_related('C"', 'Rainbow Bridge').
archetype_related('C"', 'Rose Dragon').
archetype_related('C"', 'Sacred Beast').
archetype_related('C"', 'Trap Hole').
archetype_related('C"', 'Traptrix').
archetype_related('@Ignister', 'Artifact').
archetype_related('@Ignister', 'Chaos').
archetype_related('@Ignister', 'Dogmatika').
archetype_related('@Ignister', 'Eyes Restrict').
archetype_related('@Ignister', 'Greed').
archetype_related('@Ignister', 'Guardian').
archetype_related('@Ignister', 'Harpie').
archetype_related('@Ignister', 'Herald').
archetype_related('@Ignister', 'Mulcharmy').
archetype_related('@Ignister', 'Salamangreat').
archetype_related('@Ignister', 'Tri-Brigade').
archetype_related('@Ignister', 'Voiceless Voice').
archetype_related('ABC', 'Blue-Eyes').
archetype_related('ABC', 'Chaos').
archetype_related('ABC', 'Greed').
archetype_related('ABC', 'Hole').
archetype_related('ABC', 'Horus the Black Flame Dragon').
archetype_related('ABC', 'Madoor').
archetype_related('ABC', 'Vampire').
archetype_related('ABC', 'Watt').
archetype_related('Aesir', 'Burning Abyss').
archetype_related('Aesir', 'Butterspy').
archetype_related('Aesir', 'Destiny HERO').
archetype_related('Aesir', 'Elemental HERO').
archetype_related('Aesir', 'Forbidden').
archetype_related('Aesir', 'Heroic').
archetype_related('Aesir', 'Hole').
archetype_related('Aesir', 'Junk').
archetype_related('Aesir', 'Lswarm').
archetype_related('Aesir', 'Masked HERO').
archetype_related('Aesir', 'Nordic').
archetype_related('Aesir', 'Phantom Knights').
archetype_related('Aesir', 'Scrap').
archetype_related('Aesir', 'Solemn').
archetype_related('Aether', 'Battleguard').
archetype_related('Aether', 'Draconia').
archetype_related('Aether', 'Greed').
archetype_related('Aether', 'Hole').
archetype_related('Aether', 'Magician').
archetype_related('Albaz Dragon', 'Artifact').
archetype_related('Albaz Dragon', 'Branded').
archetype_related('Albaz Dragon', 'Dogmatika').
archetype_related('Albaz Dragon', 'Greed').
archetype_related('Albaz Dragon', 'Kaiju').
archetype_related('Albaz Dragon', 'Red-Eyes').
archetype_related('Albaz Dragon', 'Springans').
archetype_related('Albaz Dragon', 'Starliege').
archetype_related('Albaz Dragon', 'Swordsoul').
archetype_related('Albaz Dragon', 'Tri-Brigade').
archetype_related('Alligator', 'Amazoness').
archetype_related('Alligator', 'Archfiend').
archetype_related('Alligator', 'Black Luster Soldier').
archetype_related('Alligator', 'Blue-Eyes').
archetype_related('Alligator', 'Book of').
archetype_related('Alligator', 'Celtic Guard').
archetype_related('Alligator', 'D.D.').
archetype_related('Alligator', 'Dark Magician').
archetype_related('Alligator', 'Dark World').
archetype_related('Alligator', 'Djinn').
archetype_related('Alligator', 'Egyptian God').
archetype_related('Alligator', 'Elemental HERO').
archetype_related('Alligator', 'Exodia').
archetype_related('Alligator', 'Eyes Restrict').
archetype_related('Alligator', 'Gaia The Fierce Knight').
archetype_related('Alligator', 'Gravekeeper\'s').
archetype_related('Alligator', 'Greed').
archetype_related('Alligator', 'Harpie').
archetype_related('Alligator', 'Hole').
archetype_related('Alligator', 'Jester').
archetype_related('Alligator', 'Jinzo').
archetype_related('Alligator', 'Junk').
archetype_related('Alligator', 'Knight').
archetype_related('Alligator', 'Koala').
archetype_related('Alligator', 'Kuriboh').
archetype_related('Alligator', 'Madoor').
archetype_related('Alligator', 'Magician').
archetype_related('Alligator', 'Mask').
archetype_related('Alligator', 'Ninja').
archetype_related('Alligator', 'Penguin').
archetype_related('Alligator', 'Red-Eyes').
archetype_related('Alligator', 'Roid').
archetype_related('Alligator', 'Silent Magician').
archetype_related('Alligator', 'Synchron').
archetype_related('Alligator', 'Toon').
archetype_related('Alligator', 'X-Saber').

% Relazioni tra race compatibili 
race_related('Psychic', 'Winged Beast').
race_related('Psychic', 'Wyrm').
race_related('Psychic', 'Zombie').
race_related('Pyro', 'Quick-Play').
race_related('Pyro', 'Reptile').
race_related('Pyro', 'Ritual').
race_related('Pyro', 'Rock').
race_related('Pyro', 'Sea Serpent').
race_related('Pyro', 'Spellcaster').
race_related('Pyro', 'Thunder').
race_related('Pyro', 'Warrior').
race_related('Pyro', 'Winged Beast').
race_related('Pyro', 'Wyrm').
race_related('Pyro', 'Yami Bakura').
race_related('Pyro', 'Yami Marik').
race_related('Pyro', 'Zombie').
race_related('Quick-Play', 'Reptile').
race_related('Quick-Play', 'Rex').
race_related('Quick-Play', 'Ritual').
race_related('Quick-Play', 'Rock').
race_related('Quick-Play', 'Sea Serpent').
race_related('Quick-Play', 'Spellcaster').
race_related('Quick-Play', 'Thunder').
race_related('Quick-Play', 'Warrior').
race_related('Quick-Play', 'Weevil').
race_related('Quick-Play', 'Winged Beast').
race_related('Quick-Play', 'Wyrm').
race_related('Quick-Play', 'Yami Bakura').
race_related('Quick-Play', 'Yami Marik').
race_related('Quick-Play', 'Yami Yugi').
race_related('Quick-Play', 'Yugi').
race_related('Quick-Play', 'Zombie').
race_related('Reptile', 'Rex').
race_related('Reptile', 'Ritual').
race_related('Reptile', 'Rock').
race_related('Reptile', 'Sea Serpent').
race_related('Reptile', 'Spellcaster').
race_related('Reptile', 'Thunder').
race_related('Reptile', 'Warrior').
race_related('Reptile', 'Weevil').
race_related('Reptile', 'Winged Beast').
race_related('Reptile', 'Wyrm').
race_related('Reptile', 'Yami Yugi').
race_related('Reptile', 'Yugi').
race_related('Reptile', 'Zombie').
race_related('Rex', 'Weevil').
race_related('Ritual', 'Rock').
race_related('Ritual', 'Sea Serpent').
race_related('Ritual', 'Spellcaster').
race_related('Ritual', 'Thunder').
race_related('Ritual', 'Warrior').
race_related('Ritual', 'Winged Beast').
race_related('Ritual', 'Yami Yugi').
race_related('Ritual', 'Yugi').
race_related('Ritual', 'Zombie').
race_related('Rock', 'Sea Serpent').
race_related('Rock', 'Spellcaster').
race_related('Rock', 'Thunder').
race_related('Rock', 'Warrior').
race_related('Rock', 'Winged Beast').
race_related('Rock', 'Wyrm').
race_related('Rock', 'Yami Yugi').
race_related('Rock', 'Zombie').
race_related('Sea Serpent', 'Spellcaster').
race_related('Sea Serpent', 'Thunder').
race_related('Sea Serpent', 'Warrior').
race_related('Sea Serpent', 'Winged Beast').
race_related('Sea Serpent', 'Wyrm').
race_related('Sea Serpent', 'Zombie').
race_related('Spellcaster', 'Thunder').
race_related('Spellcaster', 'Warrior').
race_related('Spellcaster', 'Winged Beast').
race_related('Spellcaster', 'Wyrm').
race_related('Spellcaster', 'Yami Yugi').
race_related('Spellcaster', 'Yugi').
race_related('Spellcaster', 'Zombie').
race_related('Thunder', 'Warrior').
race_related('Thunder', 'Winged Beast').
race_related('Thunder', 'Wyrm').
race_related('Thunder', 'Zombie').
race_related('Warrior', 'Winged Beast').
race_related('Warrior', 'Wyrm').
race_related('Warrior', 'Yami Yugi').
race_related('Warrior', 'Zombie').
race_related('Winged Beast', 'Wyrm').
race_related('Winged Beast', 'Yami Yugi').
race_related('Winged Beast', 'Zombie').
race_related('Wyrm', 'Zombie').
race_related('Yami Bakura', 'Yami Marik').
race_related('Yami Yugi', 'Zombie').
race_related('Yugi', 'Zombie').


