package com.lokapos.services.impl;

import com.lokapos.constants.AuthConstant;
import com.lokapos.entities.Account;
import com.lokapos.entities.OtpAndToken;
import com.lokapos.enums.OTP_AND_TOKEN_TYPE_ENUM;
import com.lokapos.enums.RESPONSE_ENUM;
import com.lokapos.exception.BadRequestException;
import com.lokapos.exception.NotAuthorizedException;
import com.lokapos.exception.NotFoundException;
import com.lokapos.exception.SystemErrorException;
import com.lokapos.model.request.ReqOtp;
import com.lokapos.model.response.ResponseGetMe;
import com.lokapos.repositories.AccountRepository;
import com.lokapos.repositories.OtpAndTokenRepository;
import com.lokapos.services.AccountService;
import com.lokapos.services.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import utils.UtilsHelper;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final HttpServletRequest httpServletRequest;
    private final OtpAndTokenRepository otpAndTokenRepository;
    private final EmailService emailService;

    @Override
    public ResponseGetMe getMe() throws NotAuthorizedException {
        try {
            Account account = getCurrentAccount();
            if (account == null) {
                throw new NotAuthorizedException(RESPONSE_ENUM.NOT_AUTHORIZED.name());
            }
            return ResponseGetMe.builder()
                    .fullName(account.getFirstName() + " " + account.getLastName())
                    .firstName(account.getFirstName())
                    .lastName(account.getLastName())
                    .email(account.getEmail())
                    .id(account.getId())
                    .build();

        } catch (Exception e) {
            throw new SystemErrorException(e);
        }
    }

    @Override
    public Account getCurrentAccount() {
        String currentUserId = httpServletRequest.getAttribute(AuthConstant.HEADER_X_WHO).toString();
        Optional<Account> account = accountRepository.findById(currentUserId);
        return account.orElse(null);
    }

    @Override
    public String verifyEmail(ReqOtp req) {
        Account account = getCurrentAccount();
        Optional<OtpAndToken> findOtp = otpAndTokenRepository.queryFindOtp(req.getOtp(), account.getId());
        if (findOtp.isEmpty()) {
            throw new BadRequestException(RESPONSE_ENUM.OTP_INVALID.name());
        }
        OtpAndToken otp = findOtp.get();
        if (otp.getExpireDate() < System.currentTimeMillis()) {
            throw new BadRequestException(RESPONSE_ENUM.OTP_EXPIRED.name());
        }
        account.setIsVerifiedEmail(true);
        accountRepository.save(account);
        otp.setActive(false);
        otpAndTokenRepository.save(otp);
        try {
            return "SUCCESS";
        } catch (Exception e) {
            throw new SystemErrorException(e);
        }
    }

    @Override
    public RESPONSE_ENUM resendVerificationEmail() {
        Account account = getCurrentAccount();
        OtpAndToken otp = otpAndTokenRepository.findByAccountIdAndActiveIsTrueAndType(account.getId(), OTP_AND_TOKEN_TYPE_ENUM.SIGN_UP_OTP).orElseThrow(() -> new NotFoundException(RESPONSE_ENUM.NOT_FOUND_OTP.name()));
        String generateOtp = UtilsHelper.generateNumericOTP();
        otp.setOtp(generateOtp);
        Long generateExpire = UtilsHelper.getExpireOnMinutes(3);
        otp.setExpireDate(generateExpire);

        otpAndTokenRepository.save(otp);
        emailService.SendingOtpSignUp(generateOtp, account);
        try {
            return RESPONSE_ENUM.SUCCESS;
        } catch (Exception e) {
            throw new SystemErrorException(e);
        }

    }
}
