package it.pagopa.pn.externalchannels.api;

import it.pagopa.pn.externalchannels.dao.VerificationCodeEntity;
import it.pagopa.pn.externalchannels.service.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import java.util.Optional;

@RestController
@RequestMapping("/external-channels/verification-code")
@RequiredArgsConstructor
@Slf4j
public class VerificationCodeController {

    private final VerificationCodeService verificationCodeService;

    @GetMapping("{digitalAddress}")
    public Mono<ResponseEntity<String>> getVerificationCode(@PathVariable String digitalAddress) {
        log.info("Start getVerificationCode for {}", digitalAddress);
        Optional<VerificationCodeEntity> resOpt = verificationCodeService.getVerificationCodeFromDb(digitalAddress);
        return resOpt.map(verificationCodeEntity -> Mono.just(ResponseEntity.ok(verificationCodeEntity.getVerificationCode()))).orElseGet(() -> Mono.just(ResponseEntity.notFound().build()));
    }
}
