package rw.rra.management.vehicles.auth;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class OtpService {
    private final Map<String, String> otpStore = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    String generateOtp(String userEmail, OtpType otpType) {
        var otp = generateOtp();
        String key = generateKey(userEmail, otp, otpType);
        storeOtp(key, otp);
        return otp;
    }

    boolean verifyOtp(String userEmail, String otp, OtpType otpType) {
        String key = generateKey(userEmail, otp, otpType);
        if (hasOtp(key)) {
            String storedOtp = getOtp(key);
            if (storedOtp.equals(otp)) {
                deleteOtp(key);
                return true;
            }
        }
        return false;
    }

    private String getOtp(String key) {
        return otpStore.get(key);
    }

    private void deleteOtp(String key) {
        otpStore.remove(key);
    }

    private boolean hasOtp(String key) {
        return otpStore.containsKey(key);
    }

    private String generateKey(String userEmail, String otp, OtpType otpType) {
        return String.format("%s:%s:%s", otpType.toString(), userEmail, otp);
    }

    private void storeOtp(String key, String otp) {
        otpStore.put(key, otp);
        // Schedule removal after 10 minutes
        scheduler.schedule(() -> deleteOtp(key), 10, TimeUnit.MINUTES);
        log.info("Storing otp is going successfully");
    }

    private String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int digit = (int) (Math.random() * 10);
            otp.append(digit);
        }
        return otp.toString();
    }
}
