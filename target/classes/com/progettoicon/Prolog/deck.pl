% Regola per verificare il numero di carte in un deck
valid_deck_size(DeckSize) :-
    min_deck_size(Min),
    max_deck_size(Max),
    DeckSize >= Min,
    DeckSize =< Max.

% Regola per verificare la quantità valida di una carta
valid_card_count(Card, Count) :-
    forbidden_card(Card),
    Count =:= 0, % Se la carta è vietata, non può essere nel deck
    !. % Taglia per evitare altre valutazioni
valid_card_count(Card, Count) :-
    limited_card(Card),
    Count =< 1, % Se la carta è limitata, massimo 1 copia
    !.
valid_card_count(Card, Count) :-
    semi_limited_card(Card),
    Count =< 2, % Se la carta è semi-limitata, massimo 2 copie
    !.
valid_card_count(_, Count) :-
    max_copies_per_card(Max),
    Count =< Max. % Altrimenti massimo 3 copie

% Regola per verificare che il deck rispetti le restrizioni
valid_deck(Deck) :-
    length(Deck, DeckSize),
    valid_deck_size(DeckSize),
    forall(member(card(CardName, Count), Deck), valid_card_count(CardName, Count)).

% Regola per contare il numero di carte di un tipo specifico in un deck
count_card_type(Type, Deck, Count) :-
    findall(1, member(card(_, _, Type), Deck), Cards),
    length(Cards, Count).

% Esempio di deck
example_deck([
    card("Dark Magician", 3, monster),
    card("Blue-Eyes White Dragon", 2, monster),
    card("Monster Reborn", 1, spell),
    card("Pot of Greed", 0, spell), % Vietata
    card("Mirror Force", 2, trap)
]).

% Esempio di verifica del deck
check_example_deck :-
    example_deck(Deck),
    valid_deck(Deck),
    writeln("Il deck è valido").