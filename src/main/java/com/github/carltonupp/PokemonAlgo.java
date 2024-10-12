package com.github.carltonupp;

import java.util.concurrent.ThreadLocalRandom;

public class PokemonAlgo {
    public static CaptureResult attempt(boolean inBattle,
                                        boolean inTrainerBattle,
                                        boolean inTutorial,
                                        int partySize,
                                        int currentBoxCapacity,
                                        boolean nonCatchableTarget,
                                        String pokeball,
                                        String targetStatus,
                                        int targetCurrentHealth,
                                        int targetMaxHealth,
                                        int targetCatchRate) {

        // sanity checks

        if (!inBattle) {
            // You can't use a ball if you aren't in a battle
            return new CaptureResult("OAK: This isn't the time to use that!");
        } else if (inTrainerBattle) {
            // You can't capture another trainer's pokemon
            return new CaptureResult("The trainer blocked the BALL! Don't be a thief!");
        } else if (!inTutorial && partySize == 6 && currentBoxCapacity == 0) {
            /*  You cannot capture a pokemon if your party and current box are full
                the exception being the catch tutorial in Viridian City where you
                don't get the pokemon */
            return new CaptureResult("The POKéMON BOX is full! Can't use that item!");
        } else if (nonCatchableTarget) {
            /* reserved for special cases such as unidentified ghosts
                or the Marowak in Pokemon Tower */
            return new CaptureResult("It dodged the thrown BALL! This POKéMON can't be caught!");
        }

        if (pokeball.equals("Master Ball")) {
            // Master Ball never fails
            return new CaptureResult(true);
        }

        // Now the actual maths

        /*
            First we need to calculate the Ball Factor, r1.
            Based on the type of ball supplied, we generate
            a random number with a different upper limit.
        */
        int upperRange = switch (pokeball) {
            case "Poké Ball" -> 255;
            case "Great Ball" -> 200;
            case "Safari Ball", "Ultra Ball" -> 150;
            default -> throw new RuntimeException("Invalid argument supplied for pokeball");
        };

        int r1 = ThreadLocalRandom.current().nextInt(0, upperRange + 1);

        /*
            Next, we need to come up with the status factor.
            Status conditions can greatly impact your chance of
            catching a Pokemon.
        */

        int s = switch (targetStatus) {
            case "SLP", "FRZ" -> 25;
            case "PSN", "BRN", "PRZ" -> 12;
            default -> 0;
        };

        // Now we need to subtract the status factor (s) from the ball factor (r1)
        int result = r1 - s;
        if (result < 0) {
            // if status factor is less than ball factor, the attempt succeeds.
            return new CaptureResult(true);
        }

        // Calculate the HP factor (f)

        int f = targetMaxHealth * 255;
        if (pokeball.equals("Great Ball")) {
            f = f / 8;
        } else {
            f = f / 12;
        }

        int chp = targetCurrentHealth / 4;
        if (chp == 0) {
            chp = 1;
        }

        f = f / chp;

        if (f > 255) {
            f = 255;
        }

        int r2 = ThreadLocalRandom.current().nextInt(0, 256);
        if (r2 <= f) {
            return new CaptureResult(true);
        }

        // calculate wobble factor
        int w = targetCatchRate * 100;
        int bf = switch (pokeball) {
            case "Poké Ball" -> 255;
            case "Great Ball" -> 200;
            case "Safari Ball", "Ultra Ball" -> 150;
            default -> throw new RuntimeException("Invalid argument supplied for pokeball");
        };

        w = w / bf;

        if (w > 255) {
            // this actually can't happen but added in as a failsafe
            throw new RuntimeException("Something went wrong");
        }

        // multiply wobble factor (w) by health factor (f)
        w = w * f;

        // divide wobble factor (w) by 255
        w = w / 255;

        int sf = switch (targetStatus) {
            case "SLP", "FRZ" -> 10;
            case "PSN", "BRN", "PRZ" -> 5;
            default -> 0;
        };

        w = w + sf;

        if (w < 10) {
            return new CaptureResult("The ball missed the POKéMON!");
        } else if (w >= 10 && w <= 29) {
            return new CaptureResult("Darn! The POKéMON broke free!");
        } else if (w >= 30 && w <= 69) {
            return new CaptureResult("Aww! It appeared to be caught!");
        } else {
            return new CaptureResult("Shoot! It was so close too!");
        }
    }
}
