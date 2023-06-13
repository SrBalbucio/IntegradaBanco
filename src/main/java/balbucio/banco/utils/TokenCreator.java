package balbucio.banco.utils;

import java.util.Random;

public class TokenCreator {

    public static String createToken(int size){
        Random random = new Random();
        int leftLimit = 48;
        int rightLimit = 122;

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(size)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return generatedString;
    }

    public static String createToken(int min, int max){
        Random random = new Random();
        int leftLimit = 48;
        int rightLimit = 122;
        int targetStringLength = NumberUtils.getRandomNumber(min, max);

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return generatedString;
    }
}
