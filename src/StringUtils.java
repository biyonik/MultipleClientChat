import java.util.Random;

public class StringUtils {
    public static String generateRandomId(int minDigits, int maxDigits) {
        Random random = new Random();
        int randomLength = random.nextInt(maxDigits - minDigits + 1) + minDigits;

        StringBuilder randomId = new StringBuilder();
        for (int i = 0; i < randomLength; i++) {
            int digit = random.nextInt(10); // 0-9 arası rastgele sayı
            randomId.append(digit);
        }

        return randomId.toString();
    }

    public static String generateNickName(String nickname) {
        String randomId = generateRandomId(2, 5);
        return nickname + "#" + randomId;
    }
}
