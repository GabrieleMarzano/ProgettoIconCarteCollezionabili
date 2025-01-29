package com.progettoicon;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import org.jpl7.Query;
import org.jpl7.Term;

import com.progettoicon.InfoCarte.ApiYGOPRODeck;
import com.progettoicon.InfoCarte.Card_Set;
import com.progettoicon.RandomForest.RandomForestClassifier;
import com.progettoicon.RandomForest.RandomForestModel;
import com.progettoicon.ReteNeurale.CardTrainingRecord;
import com.progettoicon.ReteNeurale.MainNeuralNetwork;
import com.progettoicon.ReteNeurale.VettoreCardCompatibility;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import weka.classifiers.Evaluation;
import weka.core.Instances;

public class Controll {

    private final static String[] Type = { "Effect Monster", "Trap Card", "Normal Monster", "Pendulum Effect Monster",
            "Fusion Monster",
            "Flip Effect Monster", "Spell Card", "Synchro Monster", "Tuner Monster", "Link Monster", "Token",
            "XYZ Monster", "Normal Tuner Monster",
            "Synchro Tuner Monster", "Spirit Monster", "Pendulum Tuner Effect Monster", "Union Effect Monster",
            "XYZ Pendulum Effect Monster",
            "Ritual Monster", "Ritual Effect Monster", "Gemini Monster", "Pendulum Effect Fusion Monster",
            "Toon Monster", "Pendulum Normal Monster",
            "Pendulum Effect Ritual Monster", "Pendulum Flip Effect Monster", "Synchro Pendulum Effect Monster",
            "Flip Tuner Effect Monster",
            "Skill Card" };

    private final static String[] Race = {
            "Fiend", "Dragon", "Continuous", "Zombie", "Beast-Warrior",
            "Normal", "Fairy", "Warrior", "Quick-Play", "Rock",
            "Spellcaster", "Winged Beast", "Plant", "Beast", "Machine",
            "Cyberse", "Equip", "Field", "Fish", "Thunder",
            "Pyro", "Sea Serpent", "Aqua", "Reptile", "Counter",
            "Psychic", "Insect", "Illusion", "Dinosaur", "Wyrm",
            "Ritual", "Divine-Beast", "Creator God", "", "Bonz",
            "Yugi", "Ishizu", "Mako", "Joey", "Kaiba",
            "Keith", "Rex", "Weevil", "Pegasus", "Mai",
            "Yami Yugi", "Yami Bakura", "Yami Marik", "Christine", "Emma",
            "Andrew", "David", "Odion", "Joey Wheeler", "Espa Roba",
            "Seto Kaiba", "Arkana", "Mai Valentine", "Tea Gardner", "Ishizu Ishtar",
            "Lumis Umbra", "Dr. Vellian C", "Chazz Princet", "Axel Brodie", "Yubel",
            "Jesse Anderso", "Alexis Rhodes", "Zane Truesdal", "Bastion Misaw", "Jaden Yuki",
            "Tyranno Hassl", "Aster Phoenix", "Syrus Truesda", "Paradox Broth", "Chumley Huffi",
            "Lumis and Umb", "Abidos the Th", "Titan", "Adrian Gecko", "Thelonious Vi",
            "The Supreme K", "Camula", "Nightshroud", "Don Zaloog", "Tania",
            "Amnael", "Kagemaru"
    };

    private static String[] Archetypes = {
            "Labrynth", "SPYRAL", "D/D", "Gem-",
            "Starry Knight", "Ancient Gear", "Cipher", "D.D.", "Dark Magician",
            "Performapal", "Laval", "Rikka", "Train", "Vylon",
            "Koa'ki Meiru", "Elemental HERO", "Exosister", "Inzektor", "Shinobird",
            "Mermail", "Dual Avatar", "Majestic", "T.G.", "Umi",
            "Trickstar", "Flame Swordsman", "Magistus", "Gusto", "Lswarm",
            "Stardust", "Dinomist", "Gagaga", "Exodd", "Watt",
            "Possessed", "Greed", "Invoked", "Edge Imp", "Superheavy",
            "Tearlaments", "PSY-Frame", "Kuriboh", "Malefic", "Burning Abyss",
            "Gladiator Beast", "Gadget", "Majespecter", "Alien", "Synchro",
            "Zoodiac", "Lightsworn", "Orcust", "Atlantean", "Amazoness",
            "Generaider", "Charmer", "Spellbook", "Shiranui", "Black Luster Soldier",
            "Flamvell", "Visas", "Rose", "Galaxy", "Dragunity",
            "Golden Castle of Stromberg", "Mecha Phantom Beast", "Geargia", "Boot-Up", "Traptrix",
            "B.E.S.", "Armed Dragon", "Roid", "Junk", "Hole",
            "Branded", "Tellarknight", "Dream Mirror", "Scrap", "F.A.",
            "Guardian", "Cyber Dragon", "Subterror", "Paleozoic", "Cyberdark",
            "Millennium", "Drytron", "Phantasm Spiral", "Cubic", "Herald",
            "Utopic", "Kozmo", "Salamangreat", "Icejade", "Vaalmonica",
            "Magician", "Frog", "Photon", "Koala", "Hazy",
            "Chaos", "Six Samurai", "Altergeist", "Odd-Eyes", "Fur Hire",
            "Genex", "Neo-Spacian", "Rose Dragon", "ABC", "Ninja",
            "Fire Fist", "Power Tool", "Beetrooper", "Wind-Up", "Assault Mode",
            "Chimera", "Super Quant", "Heroic", "Vendread", "Bujin",
            "Code Talker", "Morphtronic", "Gimmick Puppet", "Silent Swordsman", "Sinful Spoils",
            "Blackwing", "Worm", "Chronomaly", "Constellar", "Yang Zing",
            "Gaia The Fierce Knight", "Blue-Eyes", "Meklord", "X-Saber", "Valkyrie",
            "Book of", "Spright", "Nordic", "Gandora", "Mayakashi",
            "Abyss Actor", "Ojama", "Ghoti", "Heraldry", "Fiendsmith",
            "Fire King", "Archfiend", "Adventurer Token", "Destruction Sword", "Utopia",
            "Argostars", "Fluffal", "Crystron", "Graydle", "Ancient Warriors",
            "Nimble", "Knightmare", "Zexal", "Karakuri", "Mekk-Knight",
            "Gogogo", "Naturia", "Ashened", "Legendary Knight", "Dark World",
            "Evolsaur", "Hieratic", "Phantom Knights", "Alligator", "Arcana Force",
            "Gravekeeper's", "Melodious", "Appliancer", "Dice", "Duston",
            "Crystal", "Noble Knight", "Evil HERO", "Endymion", "Cyber Angel",
            "Ally of Justice", "CXyz", "Shaddoll", "Djinn", "Magician Girl",
            "Slime", "Flower Cardian", "Numeron", "Blaze Accelerator", "Fortune Fairy",
            "Performage", "\"C\"", "Centur-Ion", "Darklord", "Mist Valley",
            "Qli", "Venom", "Battlin' Boxer", "Infernity", "Magnet Warrior",
            "Metalfoes", "World Chalice", "Bugroth", "Yubel", "Ragnaraika",
            "Vampire", "Kashtira", "Monarch", "Harpie", "Exodia",
            "Prediction Princess", "Tenyi", "Aroma", "Gladiator", "Rokket",
            "Swordsoul", "Superheavy Samurai", "Red-Eyes", "Infinitrack", "Thunder Dragon",
            "Magical Musket", "Egyptian God", "Zefra", "Predaplant", "Earthbound",
            "Symphonic Warrior", "Myutant", "Jurrac", "Marincess", "Machina",
            "Battleguard", "White", "Penguin", "Hi-Speedroid", "Resonator",
            "Shining Sarcophagus", "Toon", "Raidraptor", "Scareclaw", "Sacred Beast",
            "Bonding", "Ice Barrier", "Frightfur", "Clear", "Swarm of",
            "Wight", "Knight", "Shark", "Parasite", "Borrel",
            "Amorphage", "Mikanko", "A-to-Z", "Amazement", "Evil Eye",
            "A.I.", "Glacial Beast", "Secret Six Samurai", "Dododo", "P.U.N.K.",
            "Timelord", "Bystial", "Nemeses", "Ryzeal", "Despia",
            "Dark Scorpion", "Guardragon", "Crystal Beast", "Dracoverlord", "Umbral Horror",
            "Vernusylph", "Dinomorphia", "Void", "Wicked God", "Fusion",
            "Seventh", "Artifact", "Springans", "Gouki", "Ghostrick",
            "Number", "Tri-Brigade", "Ancient Treasure", "Sunavalon", "Galaxy-Eyes",
            "Live☆Twin", "Super Defense Robot", "Corn", "Elemental Lord", "Demise",
            "Lyrilusc", "Fabled", "Ritual Beast", "Infernoid", "Nephthys",
            "Jester", "Sky Striker", "Deep Sea", "Gate Guardian", "Reptilianne",
            "Evoltile", "Destiny HERO", "Virtual World", "Maju", "Star Seraph",
            "Vanquish Soul", "Evil★Twin", "Gishki", "Horus the Black Flame Dragon", "Stellarknight",
            "Adamancipator", "Ogdoadic", "Jinzo", "Abyss-", "U.A.",
            "Tindangle", "Ninjitsu Art", "Witchcrafter", "Crusadia", "Snake-Eye",
            "Gaia Knight", "Azamina", "Triamid", "Fossil", "Deskbot",
            "Fleur", "Danger!", "Sylvan", "@Ignister", "War Rock",
            "Fishborg", "Therion", "Yosenju", "Trap Monster", "Krawler",
            "Fire Formation", "Purrely", "Voiceless Voice", "Time Thief", "Masked HERO"
    };

    private static String[] HumanReadableCardType = {
            "Effect Monster", "Continuous Trap", "Normal Monster", "Pendulum Effect Monster", "Normal Trap",
            "Fusion Effect Monster", "Flip Effect Monster", "Normal Spell", "Synchro Effect Monster",
            "Quick-Play Spell",
            "Tuner Effect Monster", "Link Effect Monster", "Token", "Equip Spell", "Field Spell",
            "Xyz Effect Monster", "Continuous Spell", "Tuner Normal Monster", "Counter Trap",
            "Synchro Tuner Effect Monster",
            "Fusion Monster", "Spirit Effect Monster", "Pendulum Tuner Effect Monster", "Union Effect Monster",
            "Fusion Tuner Monster",
            "Xyz Pendulum Effect Monster", "Ritual Monster", "Ritual Effect Monster", "Gemini Effect Monster",
            "Link Monster",
            "Fusion Pendulum Effect Monster", "Toon Effect Monster", "Pendulum Normal Monster", "Ritual Spell",
            "Xyz Monster",
            "Ritual Pendulum Effect Monster", "Synchro Monster", "Pendulum Flip Effect Monster", "Trap",
            "Synchro Pendulum Effect Monster",
            "Flip Tuner Effect Monster", "Pendulum Tuner Normal Monster", "Skill - Bonz", "Skill - Yugi",
            "Skill - Ishizu",
            "Skill - Mako", "Skill - Joey", "Skill - Kaiba", "Skill - Keith", "Skill - Rex",
            "Skill - Weevil", "Skill - Pegasus", "Skill - Mai", "Skill - Yami Yugi", "Skill - Yami Bakura",
            "Skill - Yami Marik", "Skill - Christine", "Skill - Emma", "Skill - Andrew", "Skill - David",
            "Skill - Odion", "Skill - Joey Wheeler", "Skill - Espa Roba", "Skill - Seto Kaiba", "Skill - Arkana",
            "Skill - Mai Valentine", "Skill - Tea Gardner", "Skill - Ishizu Ishtar", "Skill - Lumis Umbra",
            "Skill - Dr. Vellian C",
            "Skill - Chazz Princet", "Skill - Axel Brodie", "Skill - Yubel", "Skill - Jesse Anderso",
            "Skill - Alexis Rhodes",
            "Skill - Zane Truesdal", "Skill - Bastion Misaw", "Skill - Jaden Yuki", "Skill - Tyranno Hassl",
            "Skill - Aster Phoenix",
            "Skill - Syrus Truesda", "Skill - Paradox Broth", "Skill -", "Skill - Chumley Huffi",
            "Skill - Lumis and Umb",
            "Skill - Abidos the Th", "Skill - Titan", "Skill - Adrian Gecko", "Skill - Thelonious Vi",
            "Skill - The Supreme K",
            "Skill - Camula", "Skill - Nightshroud", "Skill - Don Zaloog", "Skill - Tania", "Skill - Amnael",
            "Skill - Kagemaru"
    };

    private final static String[] Attributes = { "DARK", "NULL", "LIGHT", "EARTH", "FIRE", "WIND", "WATER", "DIVINE" };

    private final static String[] BanList = { "Limited", "Semi-Limited", "Forbidden" };

    private final static double MinAtk = 0;
    private final static double MaxAtk = 5000;
    private final static double MinDef = 0;
    private final static double MaxDef = 5000;
    private final static double MinLevel = 1;
    private final static double MaxLevel = 13;
    private final static double maxDeckComuni = 10; // Es. massimo numero teorico di deck comuni
    private final static double maxCardsetComuni = 10; // Es. massimo numero teorico di set comuni

    public static double cardCompatibily(Connection conn, Carta card1, Carta card2) {
        final double PESO_EFFECT = 0.18;
        final double PESO_ARCHETYPE = 0.24;
        final double PESO_DECK_COMUNI = 0.16;
        final double PESO_RACE = 0.13;
        final double PESO_ATTRIBUTE = 0.13;
        final double PESO_CARDSET_COMUNI = 0.12;
        final double PESO_PENDULUM = 0.04;

        // Calcolo del numero di deck e set in comune (normalizzati)
        double numeroDeckComuni = (double) Database.getCommonDeckCount(conn, card1.getId(), card2.getId())
                / maxDeckComuni;
        double numeroCardsetComuni = (double) Database.getCommonCardSets(conn, card1.getId(), card2.getId())
                / maxCardsetComuni;

        // Calcolo dei punteggi parziali
        double archetypeCompatibily = archetypeCompatibily(card1.getArchetype(), card2.getArchetype(), conn);
        double raceCompatibily = raceCompatibily(card1.getRace(), card2.getRace(), conn);
        double attributeCompatibily = attributeCompatibily(card1.getAttribute(), card2.getAttribute(), conn);
        double pendulumCompatibily = pendulumCompatibily(card1.getHumanReadableCardType(),
                card2.getHumanReadableCardType());

        // Compatibilità effetto bidirezionale (già normalizzata)
        double effectCompatibilyCard1toCard2 = effectCompatibily(card1, card2);
        double effectCompatibilyCard2toCard1 = effectCompatibily(card2, card1);
        double combinedCompatibilityEffect = combineCompatibility(effectCompatibilyCard1toCard2,
                effectCompatibilyCard2toCard1);

        // Calcolo compatibilità aggregata
        double cardCompatibily = (numeroDeckComuni * PESO_DECK_COMUNI) +
                (numeroCardsetComuni * PESO_CARDSET_COMUNI) +
                (archetypeCompatibily * PESO_ARCHETYPE) +
                (raceCompatibily * PESO_RACE) +
                (attributeCompatibily * PESO_ATTRIBUTE) +
                (pendulumCompatibily * PESO_PENDULUM) +
                (combinedCompatibilityEffect);

        // Somma dei pesi
        double totalWeight = PESO_EFFECT + PESO_ARCHETYPE + PESO_DECK_COMUNI +
                PESO_RACE + PESO_ATTRIBUTE + PESO_CARDSET_COMUNI + PESO_PENDULUM;

        // Normalizzazione finale
        cardCompatibily = Math.min(cardCompatibily / totalWeight, 1.0);

        // double[] vettore = createInputVectorWithTarget(card1, card2,
        // numeroCardsetComuni,
        // numeroDeckComuni, combinedCompatibilityEffect, conn);

        // System.out.println("dimnesione vettore: " + vettore.length);

        return cardCompatibily;
    }

    public static double combineCompatibility(double card1ToCard2, double card2ToCard1) {
        // Se entrambe le carte non hanno effetto, compatibilità = 0
        if (card1ToCard2 == 0.0 && card2ToCard1 == 0.0) {
            return 0.0;
        }

        // Se una delle due carte non ha effetto
        if (card1ToCard2 == 0) {
            return card2ToCard1; // Compatibilità basata solo su card2 -> card1
        }
        if (card2ToCard1 == 0) {
            return card1ToCard2; // Compatibilità basata solo su card1 -> card2
        }

        // Entrambe le carte hanno effetto, calcolo media pesata
        double alpha = 1.0; // Peso per card1 -> card2
        double beta = 1.0; // Peso per card2 -> card1

        // Media pesata
        double combinedCompatibility = (alpha * card1ToCard2 + beta * card2ToCard1) / (alpha + beta);

        return combinedCompatibility;
    }
    // divide una stringa in frasi

    public List<String> extractSentences(String text) {
        // Configurazione e inizializzazione della pipeline
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,depparse");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // Analizza il testo e divide in frasi
        CoreDocument document = new CoreDocument(text);
        pipeline.annotate(document);
        return document.sentences().stream()
                .map(CoreSentence::text)
                .collect(Collectors.toList());
    }

    public static double pendulumCompatibily(String humanCard1, String humanCard2) {

        double compatibily = 0.0;

        // Controlla se entrambe le stringhe contengono la parola "pendulum"
        if (humanCard1 != null && humanCard2 != null
                && humanCard1.toLowerCase().contains("pendulum")
                && humanCard2.toLowerCase().contains("pendulum")) {
            compatibily = 1.0;
        }

        return compatibily;

    }

    public static double effectCompatibily(Carta card1, Carta card2) {

        double effectCompatibily = 0.0;
        double name_compatibily = 0.0; // 1 se l'effetto nomina il nome dell'altra carta
        double archetype_compatibily = 0.0; // 1 se l'effetto nomina l'archetype dell'altra carta
        double race_compatibily = 0.0; // 1 se nomina il tipo dell'altra carta cioe spell o card + continuos o rapida
        double type_compatibily = 0.0; // 1 se l'effetto nomina il tipo dell'altra carta cioe spell o card
        double attribute_compatibily = 0.0; // 1 se l'effetto nomina l'attributo dell'altra carta
        double has_effect_compatibily = 0.0; // 1 se l'effetto nomina un mostro con effetto

        // Verifico che la carta ha effetto
        if (!card1.isHas_effect()) {
            return effectCompatibily;
        } else {
            // Controllo se il nome della seconda carta non è nullo
            if (card2.getName() != null && card1.getDesc().toLowerCase().contains(card2.getName().toLowerCase())) {
                name_compatibily = 1.0;
            }

            // Controllo se l'archetipo della seconda carta non è nullo
            if (card2.getArchetype() != null
                    && card1.getDesc().toLowerCase().contains(card2.getArchetype().toLowerCase())) {
                archetype_compatibily = 1.0;
            }

            // Controllo se il tipo della seconda carta non è nullo
            if (card2.getType() != null) {
                if (card2.getType().toLowerCase().contains("trap") || card2.getType().toLowerCase().contains("spell")) {
                    // Controllo se il race e il frameType non sono nulli
                    if (card2.getRace() != null && card2.getFrameType() != null &&
                            card1.getDesc().toLowerCase().contains(
                                    card2.getRace().toLowerCase() + " " + card2.getFrameType().toLowerCase())) {
                        race_compatibily = 1.0;
                    } else if (card2.getFrameType() != null &&
                            card1.getDesc().toLowerCase().contains(card2.getFrameType().toLowerCase())) {
                        type_compatibily = 1.0;
                    }
                } else { // È un mostro
                    // Controllo se l'attributo della seconda carta non è nullo
                    if (card2.getAttribute() != null &&
                            card1.getDesc().toLowerCase().contains(card2.getAttribute().toLowerCase())) {
                        attribute_compatibily = 1.0;
                    }

                    // Verifico se la seconda carta è un mostro con effetto
                    if (card2.isHas_effect()) {
                        has_effect_compatibily = 1.0;
                    }

                    // Controllo se il race della seconda carta non è nullo
                    if (card2.getRace() != null &&
                            card1.getDesc().toLowerCase().contains(card2.getRace().toLowerCase())) {
                        race_compatibily = 1.0;
                    }

                    // Controllo se il frameType della seconda carta non è nullo
                    if (card2.getFrameType() != null &&
                            card1.getDesc().toLowerCase()
                                    .contains(card2.getFrameType().toLowerCase() + " " + "monster")) {
                        type_compatibily = 1.0;
                    }
                }
            }
        }

        // Calcolo del punteggio totale con la media pesata
        double totalScore = 0.0;
        double totalWeight = 0.0;

        // Pesatura dei diversi parametri
        Map<String, Double> weights = Map.of(
                "name", 0.35, // Peso per il nome
                "archetype", 0.35, // Peso per l'archetipo
                "race", 0.1, // Peso per il tipo + race
                "type", 0.1, // Peso per il tipo generico
                "attribute", 0.1);

        // Somma ponderata dei punteggi
        totalScore += name_compatibily * weights.get("name");
        totalScore += archetype_compatibily * weights.get("archetype");
        totalScore += race_compatibily * weights.get("race");
        totalScore += type_compatibily * weights.get("type");
        totalScore += attribute_compatibily * weights.get("attribute");

        // Calcolo del totale dei pesi
        totalWeight = weights.values().stream().mapToDouble(Double::doubleValue).sum();

        // Calcolo del punteggio finale
        effectCompatibily = totalScore / totalWeight;

        return effectCompatibily;
    }

    public static double attributeCompatibily(String attributeCard1, String attributeCard2, Connection conn) {

        double effectCompatibily = 0.0;

        if (attributeCard1 == null || attributeCard2 == null) {

            return effectCompatibily;

        } else {
            if (attributeCard1.equalsIgnoreCase(attributeCard2)) {

                effectCompatibily = 1.0;
            }

        }

        return effectCompatibily;

    }

    public static double raceCompatibily(String raceCard1, String raceCard2, Connection conn) {

        double raceCompatibily = 0.0;

        if (raceCard1 == null || raceCard2 == null) {

            return raceCompatibily;

        }

        if (raceCard1.equalsIgnoreCase(raceCard2)) {

            raceCompatibily = 1.0;
        } else {
            // Query per cercare la compatibilità nella tabella archetype_compatibili
            String query = """
                        SELECT 1
                        FROM race_compatibili
                        WHERE (race1 = ? AND race2 = ?)
                           OR (race2 = ? AND race1= ?);
                    """;

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                // Imposta i parametri della query
                pstmt.setString(1, raceCard1.toLowerCase());
                pstmt.setString(2, raceCard2.toLowerCase());
                pstmt.setString(3, raceCard1.toLowerCase()); // Controllo anche in ordine inverso
                pstmt.setString(4, raceCard2.toLowerCase());

                // Esegui la query
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        raceCompatibily = 0.15; // Se trovata, assegniamo un valore di compatibilità (es. 0.5)
                    }
                }
            } catch (SQLException e) {
                System.err.println("Errore durante il controllo della compatibilità tra archetipi: " + e.getMessage());
            }
        }

        return raceCompatibily;

    }

    public static double archetypeCompatibily(String archetypeCard1, String archetypeCard2, Connection conn) {

        double archetypeCompatibily = 0.0;

        if (archetypeCard1 == null || archetypeCard2 == null) {

            return archetypeCompatibily;

        }

        // Controllo se gli archetipi sono uguali
        if (archetypeCard1.equalsIgnoreCase(archetypeCard2)) {
            archetypeCompatibily = 1.0; // Compatibilità massima se sono uguali
        } else {
            // Query per cercare la compatibilità nella tabella archetype_compatibili
            String query = """
                        SELECT 1
                        FROM archetype_compatibili
                        WHERE (archetype1 = ? AND archetype2 = ?)
                           OR (archetype1 = ? AND archetype2 = ?);
                    """;

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                // Imposta i parametri della query
                pstmt.setString(1, archetypeCard1.toLowerCase());
                pstmt.setString(2, archetypeCard2.toLowerCase());
                pstmt.setString(3, archetypeCard2.toLowerCase()); // Controllo anche in ordine inverso
                pstmt.setString(4, archetypeCard1.toLowerCase());

                // Esegui la query
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        archetypeCompatibily = 0.5; // Se trovata, assegniamo un valore di compatibilità (es. 0.5)
                    }
                }
            } catch (SQLException e) {
                System.err.println("Errore durante il controllo della compatibilità tra archetipi: " + e.getMessage());
            }
        }

        return archetypeCompatibily;
    }

    public static double cardCompatibilyProlog(Connection conn, Carta card1, Carta card2) {
        // Carica il file Prolog
        String prologFilePath = "compatibility.pl";
        Query loadQuery = new Query("consult('" + prologFilePath + "')");
        if (!loadQuery.hasSolution()) {
            throw new RuntimeException("Impossibile caricare il file Prolog: " + prologFilePath);
        }

        // Recupera il numero di deck e set in comune
        double numeroDeckComuni = (double) Database.getCommonDeckCount(conn, card1.getId(), card2.getId())
                / maxDeckComuni;
        double numeroCardsetComuni = (double) Database.getCommonCardSets(conn, card1.getId(), card2.getId())
                / maxCardsetComuni;

        // Richiama archetype_compatibility da Prolog
        Query archetypeQuery = new Query(
                "archetype_compatibility('" + card1.getArchetype() + "', '" + card2.getArchetype() + "', Score)");
        double archetypeCompatibily = archetypeQuery.hasSolution()
                ? Double.parseDouble(archetypeQuery.oneSolution().get("Score").toString())
                : 0.0;

        // Richiama race_compatibility da Prolog
        Query raceQuery = new Query(
                "race_compatibility('" + card1.getRace() + "', '" + card2.getRace() + "', Score)");
        double raceCompatibily = raceQuery.hasSolution()
                ? Double.parseDouble(raceQuery.oneSolution().get("Score").toString())
                : 0.0;

        // Richiama attribute_compatibility da Prolog
        Query attributeQuery = new Query(
                "attribute_compatibility('" + card1.getAttribute() + "', '" + card2.getAttribute() + "', Score)");
        double attributeCompatibily = attributeQuery.hasSolution()
                ? Double.parseDouble(attributeQuery.oneSolution().get("Score").toString())
                : 0.0;

        // Richiama pendulum_compatibility da Prolog
        Query pendulumQuery = new Query(
                "pendulum_compatibility('" + card1.getHumanReadableCardType() + "', '"
                        + card2.getHumanReadableCardType() + "', Score)");
        double pendulumCompatibily = pendulumQuery.hasSolution()
                ? Double.parseDouble(pendulumQuery.oneSolution().get("Score").toString())
                : 0.0;

        // Richiama effect_compatibility da Prolog
        Query effectQuery = new Query(
                "effect_compatibility('" + card1.getName() + "', '" + card2.getName() + "', Score)");
        double combinedCompatibilityEffect = effectQuery.hasSolution()
                ? Double.parseDouble(effectQuery.oneSolution().get("Score").toString())
                : 0.0;

        // Recupera i pesi da Prolog
        Query pesoQuery = new Query("peso(effect, PesoEffect), peso(archetype, PesoArchetype), " +
                "peso(deck_comuni, PesoDeck), peso(race, PesoRace), " +
                "peso(attribute, PesoAttribute), peso(cardset_comuni, PesoCardset), " +
                "peso(pendulum, PesoPendulum)");
        // Ottieni il risultato della query come una mappa
        Map<String, Term> weights = pesoQuery.oneSolution();

        if (weights == null) {
            System.out.println("La query non ha restituito alcun risultato.");

        }

        // Recupera e converte i valori dai risultati della query
        double PESO_EFFECT = Double.parseDouble(weights.get("PesoEffect").toString());
        double PESO_ARCHETYPE = Double.parseDouble(weights.get("PesoArchetype").toString());
        double PESO_DECK_COMUNI = Double.parseDouble(weights.get("PesoDeck").toString());
        double PESO_RACE = Double.parseDouble(weights.get("PesoRace").toString());
        double PESO_ATTRIBUTE = Double.parseDouble(weights.get("PesoAttribute").toString());
        double PESO_CARDSET_COMUNI = Double.parseDouble(weights.get("PesoCardset").toString());
        double PESO_PENDULUM = Double.parseDouble(weights.get("PesoPendulum").toString());

        // Calcolo compatibilità aggregata
        double cardCompatibily = (numeroDeckComuni * PESO_DECK_COMUNI) +
                (numeroCardsetComuni * PESO_CARDSET_COMUNI) +
                (archetypeCompatibily * PESO_ARCHETYPE) +
                (raceCompatibily * PESO_RACE) +
                (attributeCompatibily * PESO_ATTRIBUTE) +
                (pendulumCompatibily * PESO_PENDULUM) +
                (combinedCompatibilityEffect);

        // Normalizzazione finale
        double totalWeight = PESO_EFFECT + PESO_ARCHETYPE + PESO_DECK_COMUNI +
                PESO_RACE + PESO_ATTRIBUTE + PESO_CARDSET_COMUNI + PESO_PENDULUM;
        return Math.min(cardCompatibily / totalWeight, 1.0);
    }

    // crea lista input per reteNeurale
    public static void creaListaINputNeuralNetwork(Connection conn) {

        List<CardTrainingRecord> records = Database.getAllCardTrainingRecords(conn); // record della tabella
                                                                                     // card_compatibily

        Carta card1;
        Carta card2;

        int i = 0;

        for (CardTrainingRecord record : records) {

            card1 = Database.getCartaById(record.getIdCard1(), conn);
            card2 = Database.getCartaById(record.getIdCard2(), conn);
            double effectCompatibily = combineCompatibility(effectCompatibily(card1, card2),
                    effectCompatibily(card2, card1));
            // Calcolo del numero di deck e set in comune (normalizzati)
            double numeroDeckComuni = (double) Database.getCommonDeckCount(conn, card1.getId(), card2.getId())
                    / maxDeckComuni;

            double numeroCardsetComuni = (double) Database.getCommonCardSets(conn, card1.getId(), card2.getId())
                    / maxCardsetComuni;

            double[] vet1 = createInputVectorWithTarget(card1, card2, effectCompatibily, numeroCardsetComuni,
                    numeroDeckComuni, conn);

            System.out.println("record rimanenti: " + (records.size() - i));

            i++;

            Database.inserisciVettoreCardCompatibily(conn, vet1, record.getCompatibility());

        }

    }

    public static VettoreCardCompatibility creaVettoreCompatibilita(Connection conn, Carta card1, Carta card2) {
        if (conn == null || card1 == null || card2 == null) {
            throw new IllegalArgumentException("Connessione, card1 o card2 non possono essere nulli.");
        }

        try {
            // Calcola l'effetto di compatibilità tra le due carte
            double effectCompatibility = combineCompatibility(
                    effectCompatibily(card1, card2),
                    effectCompatibily(card1, card2) // Modifica se necessario: potrebbe essere un altro calcolo
            );

            double numeroDeckComuni = (double) Database.getCommonDeckCount(conn, card1.getId(), card2.getId())
                    / maxDeckComuni;
            double numeroCardsetComuni = (double) Database.getCommonCardSets(conn, card1.getId(), card2.getId())
                    / maxCardsetComuni;

            // Creazione del vettore di input con il target
            double[] vet1 = createInputVectorWithTarget(card1, card2, effectCompatibility, numeroCardsetComuni,
                    numeroDeckComuni, conn);

            // Creazione e restituzione del VettoreCardCompatibility
            return new VettoreCardCompatibility(vet1);

        } catch (Exception e) {
            System.err.println("Errore durante la creazione del vettore di compatibilità: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static void CompatibilyNeuralNetworkPipeline(Connection conn, MainNeuralNetwork network) {

        List<Carta> cards = Database.getAllCarts(conn);

        int recordInseriti = 0;

        for (int i = 0; i < cards.size(); i++) {
            for (int j = i + 1; j < cards.size(); j++) {

                VettoreCardCompatibility input = creaVettoreCompatibilita(conn, cards.get(i), cards.get(j));
                double prediction = network.predict(input.getVet());

                if (prediction > 0.35) {

                    Database.insertCardCompatibility(conn, cards.get(i).getId(), cards.get(j).getId(), prediction);
                    recordInseriti++;

                }

            }

            System.out.println("Carte mancanti: " + (cards.size() - i));

        }

        System.out.print(recordInseriti);

    }

    public static void CompatibilyNeuralNetworkPipelineNewCard(Connection conn, MainNeuralNetwork network,
            List<Carta> newCards) {

        // Recupera tutte le carte dal database
        List<Carta> cards = Database.getAllCarts(conn);

        int recordInseriti = 0;

        // Itera su ogni nuova carta
        for (Carta newCard : newCards) {
            for (Carta existingCard : cards) {

                // Crea il vettore di compatibilità per la coppia di carte
                VettoreCardCompatibility input = creaVettoreCompatibilita(conn, newCard, existingCard);

                // Predice la compatibilità usando la rete neurale
                double prediction = network.predict(input.getVet());

                // Se la predizione supera la soglia, salva la compatibilità nel database
                if (prediction > 0.35) {
                    Database.insertCardCompatibility(conn, newCard.getId(), existingCard.getId(), prediction);
                    recordInseriti++;
                }
            }

            // Log per monitorare le carte rimanenti
            System.out.println("Carte rimanenti da confrontare con le nuove: "
                    + (newCards.size() - newCards.indexOf(newCard) - 1));
        }

        // Stampa il numero totale di record inseriti
        System.out.println("Totale record di compatibilità inseriti: " + recordInseriti);
    }

    // crea lista validation per reteNeurale
    // Crea lista per la validazione della rete neurale
    public static List<VettoreCardCompatibility> creaListaValidationNeuralNetwork(int batchSize, int offset,
            String tableName, Connection conn) {

        List<CardTrainingRecord> records = Database.getAllCardValidationRecords(batchSize, offset, tableName, conn); // Record
        // dalla
        // tabella
        // specificata
        List<VettoreCardCompatibility> listValidation = new ArrayList<>();
        Carta card1;
        Carta card2;

        int i = 0;

        for (CardTrainingRecord record : records) {
            card1 = Database.getCartaById(record.getIdCard1(), conn);
            card2 = Database.getCartaById(record.getIdCard2(), conn);
            double effectCompatibily = combineCompatibility(effectCompatibily(card1, card2),
                    effectCompatibily(card2, card1));

            // Calcolo del numero di deck e set in comune (normalizzati)
            double numeroDeckComuni = (double) Database.getCommonDeckCount(conn, card1.getId(), card2.getId())
                    / maxDeckComuni;
            double numeroCardsetComuni = (double) Database.getCommonCardSets(conn, card1.getId(), card2.getId())
                    / maxCardsetComuni;

            double[] vet1 = createInputVectorWithTarget(card1, card2, effectCompatibily, numeroCardsetComuni,
                    numeroDeckComuni, conn);

            VettoreCardCompatibility tmp = new VettoreCardCompatibility(vet1, record.getCompatibility());
            listValidation.add(tmp);

            System.out.println("Record rimanenti: " + (records.size() - i));
            i++;
        }

        return listValidation;
    }

    public static double[] createInputVectorWithTarget(Carta card1, Carta card2, double effectCompatibily,
            double numeroCardsetComuni, double numeroDeckComuni,
            Connection conn) {

        List<Double> inputList = new ArrayList<>();

        // Controllo e conversione dei tipi
        double[] typeCard1_conv = card1.getType() != null ? encode(card1.getType(), Type) : new double[Type.length];
        double[] typeCard2_conv = card2.getType() != null ? encode(card2.getType(), Type) : new double[Type.length];

        // Controllo e conversione della razza
        double[] raceCard1_conv = card1.getRace() != null ? encode(card1.getRace(), Race) : new double[Race.length];
        double[] raceCard2_conv = card2.getRace() != null ? encode(card2.getRace(), Race) : new double[Race.length];

        // Controllo e conversione dell'archetipo
        double[] archetypeCard1_conv = card1.getArchetype() != null ? encode(card1.getArchetype(), Archetypes)
                : new double[Archetypes.length];
        double[] archetypeCard2_conv = card2.getArchetype() != null ? encode(card2.getArchetype(), Archetypes)
                : new double[Archetypes.length];

        // Controllo e conversione del tipo leggibile
        double[] humanReadableCard1_conv = card1.getHumanReadableCardType() != null
                ? encode(card1.getHumanReadableCardType(), HumanReadableCardType)
                : new double[HumanReadableCardType.length];
        double[] humanReadableCard2_conv = card2.getHumanReadableCardType() != null
                ? encode(card2.getHumanReadableCardType(), HumanReadableCardType)
                : new double[HumanReadableCardType.length];

        // Controllo e conversione dell'attributo
        double[] attributesCard1_conv = card1.getAttribute() != null ? encode(card1.getAttribute(), Attributes)
                : new double[Attributes.length];
        double[] attributesCard2_conv = card2.getAttribute() != null ? encode(card2.getAttribute(), Attributes)
                : new double[Attributes.length];

        // Compatibilità tra deck e set (già normalizzati)
        inputList.add(numeroDeckComuni);
        inputList.add(numeroCardsetComuni);

        // Aggiungi i dati codificati per la prima carta
        for (double val : typeCard1_conv)
            inputList.add(val);
        for (double val : raceCard1_conv)
            inputList.add(val);
        for (double val : archetypeCard1_conv)
            inputList.add(val);
        for (double val : humanReadableCard1_conv)
            inputList.add(val);
        for (double val : attributesCard1_conv)
            inputList.add(val);

        // Aggiungi i dati codificati per la seconda carta
        for (double val : typeCard2_conv)
            inputList.add(val);
        for (double val : raceCard2_conv)
            inputList.add(val);
        for (double val : archetypeCard2_conv)
            inputList.add(val);
        for (double val : humanReadableCard2_conv)
            inputList.add(val);
        for (double val : attributesCard2_conv)
            inputList.add(val);

        // Compatibilità dell'effetto
        inputList.add(effectCompatibily);

        // Converte la lista in array
        double[] inputVector = new double[inputList.size()];
        for (int i = 0; i < inputList.size(); i++) {
            inputVector[i] = inputList.get(i);
        }

        // Stampa il vettore di input in una sola riga
        // System.out.print("Input Vector: ");
        // System.out.println(Arrays.toString(inputVector));

        return inputVector;
    }

    public static double[] encode(String value, String[] categories) {
        double[] encoding = new double[categories.length];
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(value)) {
                encoding[i] = 1.0;
                break;
            }
        }
        return encoding;
    }

    public static List<Carta> searchNewCardFromApi(Connection conn) {

        // Recupera tutte le carte dall'API
        List<Carta> cards = ApiYGOPRODeck.getCardInfo();

        // Lista per memorizzare le nuove carte
        List<Carta> newCards = new ArrayList<>();

        // Recupera gli ID delle carte presenti nel database
        Set<Integer> dbCardIds = Database.getAllCardIds(conn);

        // Verifica se una carta è nuova
        for (Carta card : cards) {
            if (!dbCardIds.contains(card.getId())) {
                newCards.add(card);
                // Stampa informazioni sulla nuova carta
                System.out.println("Nuova carta trovata:");
                System.out.println("ID: " + card.getId());
                System.out.println("Nome: " + card.getName());
                System.out.println("Tipo: " + card.getType());
                System.out.println("Razza: " + card.getRace());
                System.out.println("ATK: " + card.getAtk());
                System.out.println("DEF: " + card.getDef());
                System.out.println("Livello: " + card.getLevel());
                System.out.println("Archetipo: " + card.getArchetype());
                System.out.println("----------------------------------");
            }
        }

        System.out.println("numero nuove carte: " + newCards.size());
        // Ritorna la lista delle nuove carte trovate
        return newCards;
    }

    public static List<Card_Set> searchNewCardSetFromApi(Connection conn) {
        // Recupera tutti i card_sets dall'API
        List<Card_Set> cardSets = ApiYGOPRODeck.getCardSet();

        // Lista per memorizzare i nuovi card_sets
        List<Card_Set> newCardSet = new ArrayList<>();

        // Recupera le coppie di set_code e set_name presenti nel database
        Set<Map.Entry<String, String>> dbCardSetIds = Database.getAllCardSetIds(conn);

        // Verifica se un card_set è nuovo
        for (Card_Set cardSet : cardSets) {
            Map.Entry<String, String> currentEntry = Map.entry(cardSet.getSet_code(), cardSet.getSet_name());
            if (!dbCardSetIds.contains(currentEntry)) {
                newCardSet.add(cardSet);
            }
        }

        // Stampa i nuovi card_sets
        for (Card_Set newSet : newCardSet) {
            System.out.println("Nuovo Card Set trovato: " + newSet.getSet_name() + " (" + newSet.getSet_code() + ")");
        }

        return newCardSet;
    }

    public static double normalize(int value, double min, double max) {
        return ((double) (value - min) / (max - min));
    }

    public static double convert(boolean value) {
        return value ? 1.0 : 0.0;
    }

    public String[] getType() {
        return Type;
    }

    public double getMinAtk() {
        return MinAtk;
    }

    public double getMaxAtk() {
        return MaxAtk;
    }

    public static void AggiornaConoscenza(Connection conn, MainNeuralNetwork network) {
        // Recupera la lista dei nuovi set di carte dall'API confrontandoli con quelli
        // nel database
        List<Card_Set> newCardSets = Controll.searchNewCardSetFromApi(conn);
        // Recupera la lista delle nuove carte dall'API confrontandole con quelle nel
        // database
        List<Carta> newCarte = Controll.searchNewCardFromApi(conn);

        // Inserisce i nuovi set di carte nel database
        Database.insertNewCardSetDb(conn, newCardSets);

        // Inserisce le nuove carte nel database
        Database.insertNewCardList(conn, newCarte);

        // Inserisce i card_set nella tabella newcard_sets
        for (Card_Set card_sets : newCardSets) {
            int id = Database.getCardSetIdByNameAndCode(conn, card_sets.getSet_name(), card_sets.getSet_code());
            Database.inserisciInNewCard_sets(conn, id);
        }

        // Inserisce le carte nella tabella newcard
        for (Carta card : newCarte) {
            Database.inserisciInNewCard(conn, card.getId());
        }

        // Aggiorna il campo "has_effect" per le nuove carte nel database
        Database.updateHAS_EFFECT(conn, newCarte);

        // Inserisce le associazioni tra carte e i loro set nel database
        Database.inserisciCardSetsPerCarte(conn, newCarte);

        // Inserisce i prezzi per le carte nei set
        for (Carta card : newCarte) {
            try {
                Database.InserisciPriceForCard_CardsetsSingola(card, conn);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        // aggiorna i prezzi in card_cardset
        try {
            Database.InserisciPriceForCard_Cardsets(conn);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        CompatibilyNeuralNetworkPipelineNewCard(conn, network, newCarte);
        // Elimina le carte vecchie da newCard
        Database.eliminaCarteVecchieDaNewCard(conn);

        // Elimina i set di carte vecchi da newCard
        Database.eliminaCard_setsVecchiDaNewCard(conn);
    }

    public static void runRandomForestPipeline(Database db_yu_gi_oh, String trainingDataPath, String testDataPath,
            int numTrees, int maxDepth) {
        try {
            // Creazione dell'oggetto RandomForestModel
            RandomForestModel randomForestModel = new RandomForestModel();

            // Verifica se il modello esiste
            if (randomForestModel.isModelLoaded()) {
                System.out.println("Modello esistente caricato con successo.");
            } else {
                System.out.println("Modello non trovato. Addestramento in corso...");

                // Carica i dati di addestramento dal file ARFF
                Instances trainingData = randomForestModel.loadDataFromArff(trainingDataPath);
                if (trainingData == null) {
                    System.err.println("Errore durante il caricamento dei dati di addestramento.");
                    return;
                }

                // Imposta l'ultima colonna come target (valore da prevedere)
                trainingData.setClassIndex(trainingData.numAttributes() - 1);

                // Addestra il modello
                randomForestModel.trainModel(trainingData, numTrees, maxDepth);
                System.out.println("Modello addestrato e salvato con successo.");
            }

            // Carica i dati di test
            Instances testData = randomForestModel.loadDataFromArff(testDataPath);

            if (testData != null && testData.numInstances() > 0) {
                testData.setClassIndex(testData.numAttributes() - 1); // Imposta l'ultima colonna come target

                try {
                    // Valutazione del modello
                    if (!randomForestModel.isModelLoaded()) {
                        throw new IllegalStateException("Il modello non è stato addestrato.");
                    }

                    // Esegui la valutazione per la regressione
                    Evaluation evaluation = new Evaluation(testData);
                    evaluation.evaluateModel(randomForestModel.getModel(), testData);

                    // Stampa delle metriche di regressione
                    System.out.println("=== Valutazione del Modello ===");
                    System.out.println("RMSE: " + evaluation.rootMeanSquaredError());
                    System.out.println("MAE: " + evaluation.meanAbsoluteError());
                    System.out.println("R²: " + evaluation.correlationCoefficient());
                } catch (Exception e) {
                    System.err.println("Errore durante la valutazione del modello: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.err.println("Errore: il set di test non contiene istanze valide.");
            }
        } catch (Exception e) {
            System.err.println("Errore durante l'esecuzione della pipeline: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void runRandomForestClassifierPipeline(Database db_yu_gi_oh, String trainingDataPath,
            String testDataPath,
            int numTrees, int maxDepth) {
        try {
            // Creazione dell'oggetto RandomForestClassifier
            RandomForestClassifier randomForestClassifier = new RandomForestClassifier();

            // Verifica se il modello esiste
            if (randomForestClassifier.isModelLoaded()) {
                System.out.println("Modello esistente caricato con successo.");
            } else {
                System.out.println("Modello non trovato. Addestramento in corso...");

                // Carica i dati di addestramento dal file ARFF
                Instances trainingData = randomForestClassifier.loadDataFromArff(trainingDataPath);
                if (trainingData == null) {
                    System.err.println("Errore durante il caricamento dei dati di addestramento.");
                    return;
                }

                // Imposta l'ultima colonna come target (valore da prevedere)
                trainingData.setClassIndex(trainingData.numAttributes() - 1);

                // Addestra il modello
                randomForestClassifier.trainModel(trainingData, numTrees, maxDepth);
                System.out.println("Modello di classificazione addestrato e salvato con successo.");
            }

            // Carica i dati di test
            Instances testData = randomForestClassifier.loadDataFromArff(testDataPath);

            if (testData != null && testData.numInstances() > 0) {
                testData.setClassIndex(testData.numAttributes() - 1); // Imposta l'ultima colonna come target

                try {
                    // Valutazione del modello
                    if (!randomForestClassifier.isModelLoaded()) {
                        throw new IllegalStateException("Il modello non è stato addestrato.");
                    }

                    Evaluation evaluation = new Evaluation(testData);
                    evaluation.evaluateModel(randomForestClassifier.getModel(), testData);

                    // Stampa delle metriche di classificazione
                    System.out.println("=== Valutazione del Modello ===");
                    System.out
                            .println("Accuratezza: " + (evaluation.correct() / evaluation.numInstances()) * 100 + "%");
                    System.out.println("Precisione: " + evaluation.weightedPrecision());
                    System.out.println("Recall: " + evaluation.weightedRecall());
                    System.out.println("F1-Score: " + evaluation.weightedFMeasure());
                    System.out.println("Confusion Matrix: ");
                    System.out.println(evaluation.toMatrixString());

                    // Predizione per ogni istanza del set di test
                    // for (int i = 0; i < testData.numInstances(); i++) {
                    // String predictedClass = randomForestClassifier.predict(testData.get(i)); //
                    // // Classe predetta
                    // String actualClass = testData.classAttribute().value((int)
                    // testData.get(i).classValue()); // Classe
                    // // effettiva

                    // System.out.println("Istanza " + (i + 1) + ":");
                    // System.out.println(" Classe Predetta: " + predictedClass);
                    // System.out.println(" Classe Effettiva: " + actualClass);
                    // }

                    classificationPipeline(db_yu_gi_oh.getConn(), randomForestClassifier);
                } catch (Exception e) {
                    System.err.println("Errore durante la valutazione del modello: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.err.println("Errore: il set di test non contiene istanze valide.");
            }
        } catch (Exception e) {
            System.err.println("Errore durante l'esecuzione della pipeline: " + e.getMessage());
        }
    }

    public static int InserisciIDCard() {
        Scanner scanner = new Scanner(System.in);
        int number = 0;
        boolean isValid = false;

        System.out.println("Inserisci l'id di una carta:");

        while (!isValid) {
            try {
                // Legge la linea di input
                String input = scanner.nextLine();

                // Prova a convertire l'input in un numero
                number = Integer.parseInt(input);

                // Se la conversione ha successo, il numero è valido
                isValid = true;

            } catch (NumberFormatException e) {
                // Gestisce il caso in cui l'input non è un numero
                System.err.println("Errore: Input non valido. Inserisci un numero intero.");
            }
        }

        return number;
    }

    public static void classificationPipeline(Connection conn, RandomForestClassifier randomForestClassifier) {
        if (randomForestClassifier == null || conn == null) {
            System.err.println("Errore: Modello o connessione al database non validi.");
            return;
        }

        // Verifica se il modello è caricato
        if (randomForestClassifier.isModelLoaded()) {
            System.out.println("Modello esistente caricato con successo.");

            int id = Controll.InserisciIDCard();

            List<Carta> cards = Database.getCardsByCardId(conn, id);

            if (!cards.isEmpty()) {

                System.out.println("=== Informazioni della Carta ===");
                Carta card = cards.get(0);
                System.out.println("ID: " + card.getId());
                System.out.println("Nome: " + card.getName());
                System.out.println("Tipo: " + card.getType());
                System.out.println("Razza: " + card.getRace());
                System.out.println("Attacco: " + card.getAtk());
                System.out.println("Difesa: " + card.getDef());
                System.out.println("Livello: " + card.getLevel());
                System.out.println("Archetipo: " + card.getArchetype());
                System.out.println("Attributo: " + card.getAttribute());
                System.out.println("Descrizione: " + card.getDesc());
                System.out.println("URL Immagine: " +
                        (card.getCard_images() != null && !card.getCard_images().isEmpty()
                                ? card.getCard_images().get(0).getImage_url()
                                : "N/A"));
                System.out.println("=== Fine Informazioni della Carta ===");

                for (Carta currentCard : cards) {
                    if (!currentCard.getCard_sets().isEmpty()) {
                        System.out.println("=== Informazioni del CardSet ===");

                        List<Double> dynamicArray = new ArrayList<>();

                        // Aggiungi valori
                        dynamicArray.add((double) currentCard.getCard_sets().get(0).getPrices().getAverage());
                        dynamicArray.add((double) currentCard.getCard_sets().get(0).getPrices().getShift());
                        dynamicArray.add((double) currentCard.getCard_sets().get(0).getPrices().getShift_3());
                        dynamicArray.add((double) currentCard.getCard_sets().get(0).getPrices().getShift_7());
                        dynamicArray.add((double) currentCard.getCard_sets().get(0).getPrices().getShift_21());
                        dynamicArray.add((double) currentCard.getCard_sets().get(0).getPrices().getShift_30());
                        dynamicArray.add((double) currentCard.getCard_sets().get(0).getPrices().getShift_90());
                        dynamicArray.add((double) currentCard.getCard_sets().get(0).getPrices().getShift_180());
                        dynamicArray.add((double) currentCard.getCard_sets().get(0).getPrices().getShift_365());

                        // Converti in array statico
                        Double[] cardAttributes = dynamicArray.toArray(new Double[0]);

                        System.out.println("Nome Set: " + currentCard.getCard_sets().get(0).getSet_name());
                        System.out.println("Codice Set: " + currentCard.getCard_sets().get(0).getSet_code());
                        System.out.println("Rarità: " + currentCard.getCard_sets().get(0).getSet_rarity());
                        System.out.println("Codice Rarità: " + currentCard.getCard_sets().get(0).getSet_rarity_code());
                        System.out.println("Prezzo Set: " + currentCard.getCard_sets().get(0).getSet_price());

                        System.out.println(
                                "Average Price: " + currentCard.getCard_sets().get(0).getPrices().getAverage() + ", " +
                                        "Shift: " + currentCard.getCard_sets().get(0).getPrices().getShift() + ", " +
                                        "Shift_3: " + currentCard.getCard_sets().get(0).getPrices().getShift_3() + ", "
                                        +
                                        "Shift_7: " + currentCard.getCard_sets().get(0).getPrices().getShift_7() + ", "
                                        +
                                        "Shift_21: " + currentCard.getCard_sets().get(0).getPrices().getShift_21()
                                        + ", " +
                                        "Shift_30: " + currentCard.getCard_sets().get(0).getPrices().getShift_30()
                                        + ", " +
                                        "Shift_90: " + currentCard.getCard_sets().get(0).getPrices().getShift_90()
                                        + ", " +
                                        "Shift_180: " + currentCard.getCard_sets().get(0).getPrices().getShift_180()
                                        + ", " +
                                        "Shift_365: " + currentCard.getCard_sets().get(0).getPrices().getShift_365());

                        // Classificazione
                        String prediction = RandomForestClassifier.classifyCard(randomForestClassifier, cardAttributes);
                        if ("prezzo diminuisce".equals(prediction)) {
                            System.out.println("\u001B[31m" + prediction + "\u001B[0m"); // Rosso
                        } else if ("prezzo aumenta".equals(prediction)) {
                            System.out.println("\u001B[32m" + prediction + "\u001B[0m"); // Verde
                        } else {
                            System.out.println("Predizione sconosciuta: " + prediction);
                        }

                        System.out.println("=== Fine Informazioni del CardSet ===");
                    } else {
                        System.out.println("Dati non disponibili per classificare la Carta.");
                    }
                }

            } else {
                System.out.println("Carta non trovata.");
            }

        } else {
            System.out.println("Modello non caricato.");
        }
    }

}
