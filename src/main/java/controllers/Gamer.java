/**
 * Arshdeep Singh
 * TCSS 559 Fall quarter
 * Assignment 6
 * December 9, 2020.
 */
package controllers;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a class built for assignment 6.
 *
 * The theme is video games.
 * I have re-offered 4 services which I found online for free.
 * Service 1: comparison of Apex players.
 * Service 2: character weaknesses for Pokemon Go.
 * Service 3: stat comparison for handheld Pokemon games.
 * Service 4: game ratings and similar games.
 */
// Java class that will host the URI path "/"
@Path("/")
public class Gamer {



    /**Wrapped Service 1.
     *
     * This service takes in the usernames of 2 players who play Apex on Playstation 4.
     * It then compares those two users and finds out which one is better.
     *
     * @param userName the first players playstation username.
     * @param userName2 the second players playstation username.
     * @return a decision about which player is better at the game. And which character (legend) each uses (in the header).
     * @throws UnirestException
     */
    @Path("/apex/{username}/{username2}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApexStats(@PathParam("username") String userName, @PathParam("username2") String userName2) throws UnirestException {

        HttpResponse<String> response = Unirest.get("https://api.mozambiquehe.re/bridge?version=4&platform=PS4&player=" + userName + "&auth=qT39mgGbPKOCV07sUp2Q")
                .asString();
        HttpResponse<String> response2 = Unirest.get("https://api.mozambiquehe.re/bridge?version=4&platform=PS4&player=" + userName2 + "&auth=qT39mgGbPKOCV07sUp2Q")
                .asString();

        try {
            JSONObject jOb = new JSONObject(response.getBody());
            JSONObject global = jOb.getJSONObject("global");
            int level = Integer.parseInt(global.getString("level"));
            JSONObject rank = global.getJSONObject("rank");
            int rankLevel = Integer.parseInt(rank.getString("rankScore"));
            JSONObject legend = jOb.getJSONObject("legends");
            JSONObject selected = legend.getJSONObject("selected");
            String legendName = selected.getString("LegendName");

            JSONObject jOb2 = new JSONObject(response2.getBody());
            JSONObject global2 = jOb2.getJSONObject("global");
            int level2 = Integer.parseInt(global2.getString("level"));
            JSONObject rank2 = global2.getJSONObject("rank");
            int rankLevel2 = Integer.parseInt(rank2.getString("rankScore"));
            JSONObject legend2 = jOb2.getJSONObject("legends");
            JSONObject selected2 = legend2.getJSONObject("selected");
            String legendName2 = selected2.getString("LegendName");

            apexObject aO = new apexObject(level, level2, rankLevel, rankLevel2, userName, userName2);

            return Response.ok(aO).header("ARSH_player1_Legend", legendName).header("ARSH_player2_Legend", legendName2).build();

        } catch (Exception e) {
            errorObject eO = new errorObject(e);
            return Response
                    .status(Response.Status.OK)
                    .entity(eO)
                    .build();
        }
    }

    /**
     * Entity object for the first wrapped service.
     */
    public class apexObject {
        int Player1Level;
        int Player2Level;
        int LevelDifference;
        String BetterPlayer;
        int Player1RankScore;
        int Player2RankScore;
        int RankScoreDifference;

        public apexObject(int level1, int level2, int rank1, int rank2, String p1, String p2) {
            Player1Level = level1;
            Player2Level = level2;
            LevelDifference = Math.abs(level1 - level2);
            Player1RankScore = rank1;
            Player2RankScore = rank2;
            RankScoreDifference = Math.abs(rank1 - rank2);

            if (level1 > level2 && rank1 > rank2) {
                BetterPlayer = p1;
            } else if (level2 > level1 && rank1 > rank2){
                BetterPlayer = "Equals";
            } else if (level1 > level2 && rank2 > rank1) {
                BetterPlayer = "Equals";
            } else {
                BetterPlayer = p2;
            }
        }

        public int getLevelDifference() { return LevelDifference; }
        public int getPlayer1Level() { return Player1Level;}
        public int getPlayer2Level() { return Player2Level;}
        public int getRankScoreDifference() { return RankScoreDifference;}
        public int getPlayer1RankScore() { return Player1RankScore;}
        public int getPlayer2RankScore() { return Player2RankScore;}
        public String getBetterPlayer() {return BetterPlayer;}
    }

    /**
     * Error object which can be returned in the response if there is any error while calling my services.
     */
    public class errorObject {
        String error;
        public errorObject(Exception e) {
            error = e.toString();
        }

        public String getError() {
            return error;
        }
    }


    /** Wrapped service 2.
     *
     * This service returns some basic type info about the Pokemon.
     * It also returns the weakness of the Pokemon (in the header).
     *
     * @param theNumber is the id number of the Pokemon.
     * @return some basic info and weakness list in the header.
     */
    @Path("/PokeWeak/{number}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPokeWeak(@PathParam("number") int theNumber) {
        try {
            HttpResponse<String> response = Unirest.get("https://pokemon-go1.p.rapidapi.com/pokemon_types.json")
                    .header("x-rapidapi-key", "edbe30d068msh93815234e01baa5p19668fjsn8d02bb60fecd")
                    .header("x-rapidapi-host", "pokemon-go1.p.rapidapi.com")
                    .asString();

            JSONArray jAr = new JSONArray(response.getBody());

            List<Pokemon> pokemonList = new ArrayList<>();
            for (int i = 0; i < jAr.length(); i++) {
                JSONObject jO = jAr.getJSONObject(i);
                String form = jO.getString("form");
                if (form.equals("Normal")) {
                    Pokemon p = new Pokemon(jO.getString("pokemon_name"),
                            jO.getString("pokemon_id"),
                            jO.getJSONArray("type") );
                    pokemonList.add(p);
                }
            }

            return Response.ok(pokemonList.get(theNumber - 1))
                    .header("ARSH_pokemon_weakness",
                            getWeakString(getWeaknesses(pokemonList.get(theNumber - 1).getType1())))
                    .build();

        } catch (Exception e) {
            errorObject eO = new errorObject(e);
            return Response
                    .status(Response.Status.OK)
                    .entity(eO)
                    .build();
        }
    }

    /** This is used to create a string of the list of weaknesses.
     *
     * @param weaknesses array of weakness.
     * @return string sentence of the array.
     */
    private String getWeakString(String[] weaknesses) {
        String sentence = "";
        for (int i = 0; i < weaknesses.length; i++ ){
            sentence += weaknesses[i] + " ";
        }

        return sentence;
    }

    /**
     * Entity object to be used by Wrapped service 2.
     */
    public class Pokemon {
        String PokemonName;
        int ID;
        String Type1;
        String Type2;

        public Pokemon(String pokemon_name, String pokemon_id, JSONArray type) {
            PokemonName = pokemon_name;
            ID = Integer.parseInt(pokemon_id);
            Type1 = type.get(0).toString();
            if (type.length() == 2) {
                Type2 = type.get(1).toString();
            } else{
                Type2 = "no secondary type";
            }
        }

        public String getPokemonName() {return PokemonName;}
        public int getID() {return ID; }
        public String getType1() {return Type1;}
        public String getType2() {return  Type2;}
    }

    /** Each Pokemon has a weakness depending on it's type.
     *  Some types have multiple weaknesses.
     *
     * @param type is the type of the Pokemon who's weaknesses need to be found.
     * @return the list of weaknesses in an array.
     */
    public String[] getWeaknesses(String type) {
        String[] ws =  type.equals("Grass") ? new String[]{"fire", "ice", "flying", "poison", "bug"} :
                type.equals("Fire") ? new String[]{"water", "ground", "rock"} :
                        type.equals("Water") ? new String[]{"electric", "grass"} :
                                type.equals("Electric") ? new String[] {"ground"} :
                                        type.equals("Normal") ? new String[] {"fighting"} :
                                                type.equals("Bug") ? new String[]{"fire", "poison", "flying", "psychic"} :
                                                        type.equals("Poison") ? new String[] {"psychic", "ground"} :
                                                                type.equals("Psychic") ? new String[] {"bug", "ghost", "dark"} :
                                                                        type.equals("Dark") ? new String[] {"dark", "fairy", "fighting" } :
                                                                                type.equals("Dragon") ? new String[] {"dragon", "ice", "fairy" } :
                                                                                        type.equals("Fairy") ? new String[] {"poison", "steel" } :
                                                                                                type.equals("Fighting") ? new String[] {"fairy", "flying", "psychic" } :
                                                                                                        type.equals("Flying") ? new String[] {"electric", "ice", "rock" } :
                                                                                                                type.equals("Ghost") ? new String[] {"dark", "ghost" } :
                                                                                                                        type.equals("Ice") ? new String[] {"fighting", "fire", "rock", "steel" } :
                                                                                                                                type.equals("Rock") ? new String[] {"fighting", "steel", "grass", "ground", "water" } :
                                                                                                                                        type.equals("Steel") ? new String[] {"fighting", "fire", "ground" } :
                                                                                                                                                type.equals("Ground") ? new String[] {"grass", "ice", "water" } :
                                                                                                                                                        new String[] {"NONE"};
        return ws;
    }



    /** Wrapped service 3:
     *
     * Gets the fighting statistics of two pokemon and compares them.
     *
     * @param theNumber1 the id number of the first pokemon
     * @param theNumber2 the id number of the second pokemon
     * @return the difference between the statistics.
     *          Positive values favor first pokemon. Negative values favor second pokemon.
     */
    @Path("/PokeStats/{number1}/{number2}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPokeStats(@PathParam("number1") int theNumber1, @PathParam("number2") int theNumber2) {
        try {
            HttpResponse<String> response1 = Unirest.get("https://pokeapi.co/api/v2/pokemon/" + theNumber1)
                    .asString();
            HttpResponse<String> response2 = Unirest.get("https://pokeapi.co/api/v2/pokemon/" + theNumber2)
                    .asString();

            Pokemon_v2 ps1 = getPokemon_v2(response1);
            Pokemon_v2 ps2 = getPokemon_v2(response2);

            PokeCompareObject PCO = new PokeCompareObject(ps1, ps2);

            DecimalFormat df = new DecimalFormat("0.0");

            return Response.ok(PCO)
                    .header("ARSH_pokemon1_weight", df.format(ps1.Weight / 4.53) + " pounds")
                    .header("ARSH_pokemon2_weight", df.format(ps2.Weight /4.53) + " pounds")
                    .build();

        } catch(Exception e) {
            errorObject eO = new errorObject(e);
            return Response
                    .status(Response.Status.OK)
                    .entity(eO)
                    .build();
        }
    }

    /**
     * Entity object for service 3.
     */
    public class PokeCompareObject {
        int hp_diff;
        int attack_diff;
        int defense_diff;
        int sp_attack_diff;
        int sp_defense_diff;
        int speed_diff;

        public PokeCompareObject(Pokemon_v2 p1, Pokemon_v2 p2) {
            hp_diff = p1.stat_HP - p2.stat_HP;
            attack_diff = p1.stat_Attack - p2.stat_Attack;
            defense_diff = p1.stat_Defense - p2.stat_Defense;
            sp_attack_diff = p1.stat_SPAtack - p2.stat_SPAtack;
            sp_defense_diff = p1.stat_SPDefense - p2.stat_SPDefense;
            speed_diff = p1.stat_Speed - p2.stat_Speed;
        }

        public int getHp_diff() {
            return hp_diff;
        }
        public int getDefense_diff() {
            return defense_diff;
        }
        public int getSp_defense_diff() {
            return sp_defense_diff;
        }
        public int getSpeed_diff() {
            return speed_diff;
        }
        public int getAttack_diff() {
            return attack_diff;
        }
        public int getSp_attack_diff() {
            return sp_attack_diff;
        }
    }

    /** Private class for wrapped service 3.
     *
     * Does the work of getting values out of the HTTP response.
     *
     * @param response1 is the http response given by calling the URI.
     * @return an object holding the various values.
     */
    private Pokemon_v2 getPokemon_v2(HttpResponse<String> response1) {
        JSONObject jO1 = new JSONObject(response1.getBody());
        JSONObject species1 = jO1.getJSONObject("species");
        String name1 = species1.getString("name");
        int weight = jO1.getInt("weight");
        JSONArray stats = jO1.getJSONArray("stats");
        JSONObject jhp = stats.getJSONObject(0);
        JSONObject jatt = stats.getJSONObject(1);
        JSONObject jdef = stats.getJSONObject(2);
        JSONObject jsps = stats.getJSONObject(3);
        JSONObject jspd = stats.getJSONObject(4);
        JSONObject jsp = stats.getJSONObject(5);

        Pokemon_v2 ps1 = new Pokemon_v2(weight, jhp.getInt("base_stat"), jatt.getInt("base_stat"),
                jdef.getInt("base_stat"), jsps.getInt("base_stat"),
                jspd.getInt("base_stat"), jsp.getInt("base_stat") );
        return ps1;
    }

    /**
     *  Another object for wrapped service 3. It holds some values for comparison at a later point.
     */
    public class Pokemon_v2 {
        int Weight;
        int stat_HP;
        int stat_Attack;
        int stat_Defense;
        int stat_SPAtack;
        int stat_SPDefense;
        int stat_Speed;

        public Pokemon_v2(int weight, int hp, int att, int def, int spa, int spd, int sp) {
            Weight = weight;
            stat_Attack = att;
            stat_Defense = def;
            stat_HP = hp;
            stat_SPAtack = spa;
            stat_SPDefense = spd;
            stat_Speed = sp;
        }
    }


    /** Wrapped service 4:
     *
     * Gets the ratings for a video game and gets the rating for similar video games.
     *
     * @param theGame the name of a video game
     * @return a response in json
     */
    @Path("/Ratings/{game}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGameScores(@PathParam("game") String theGame) {
        String built ="";
        try {
            HttpResponse<String> response1 = Unirest.get("https://api.rawg.io/api/games?page_size=2&search=" + theGame)
                    .asString();
            built += theGame;
            HttpResponse<String> response2 = Unirest.get("https://api.rawg.io/api/games/" + theGame + "/suggested?page_size=3")
                    .asString();

            JSONObject jO = new JSONObject(response1.getBody());
            JSONArray jA = jO.getJSONArray("results");
            JSONObject jA0 = jA.getJSONObject(0);
            double rating = jA0.getDouble("rating");
            String name = jA0.getString("name");
            JSONArray rats = jA0.getJSONArray("ratings");
            JSONObject rats0 = rats.getJSONObject(0);
            int mostPopScore = rats0.getInt("id");
            double popScorePercent = rats0.getDouble("percent");

            JSONObject jO2 = new JSONObject(response2.getBody());
            JSONArray jA2 = jO2.getJSONArray("results");
            JSONObject jA20 = jA2.getJSONObject(0);
            String nameG1 = jA20.getString("name");
            double ratG1 = jA20.getDouble("rating");
            JSONObject jA21 = jA2.getJSONObject(1);
            String nameG2 = jA21.getString("name");
            double ratG2 = jA21.getDouble("rating");
            JSONObject jA22 = jA2.getJSONObject(2);
            String nameG3 = jA22.getString("name");
            double ratG3 = jA22.getDouble("rating");

            GamesObject gamesObject = new GamesObject(name, rating, nameG1, ratG1, nameG2, ratG2, nameG3, ratG3);

            return Response.ok(gamesObject)
                    .header("ARSH_Most_Popular_Score", mostPopScore + "/5, voted by " + popScorePercent + "%")
                    .build();

        } catch (Exception e) {
            errorObject eO = new errorObject(e);
            return Response
                    .status(Response.Status.OK)
                    .entity(eO)
                    .header("built", built)
                    .build();
        }
    }

    /**
     * response entity for Service 4
     */
    public class GamesObject {
        String Game;
        double Rating;
        String SimilarGame1;
        double SimilarGame1_rating;
        String SimilarGame2;
        double SimilarGame2_rating;
        String SimilarGame3;
        double SimilarGame3_rating;

        public GamesObject(String game, double rating, String sim1, double r1, String sim2, double r2, String sim3, double r3) {
            Game = game;
            Rating = rating;
            SimilarGame1 = sim1;
            SimilarGame1_rating = r1;
            SimilarGame2 = sim2;
            SimilarGame2_rating = r2;
            SimilarGame3 = sim3;
            SimilarGame3_rating = r3;
        }

        public String getGame() {
            return Game;
        }

        public double getRating() {
            return Rating;
        }

        public String getSimilarGame1() {
            return SimilarGame1;
        }

        public double getSimilarGame1_rating() {
            return SimilarGame1_rating;
        }

        public String getSimilarGame2() {
            return SimilarGame2;
        }

        public double getSimilarGame2_rating() {
            return SimilarGame2_rating;
        }

        public String getSimilarGame3() {
            return SimilarGame3;
        }

        public double getSimilarGame3_rating() {
            return SimilarGame3_rating;
        }
    }



}