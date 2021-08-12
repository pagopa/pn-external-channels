/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pagopa.pn.externalchannels.api;

import it.pagopa.pn.model.event.IPnExtChnPecEvent;
import it.pagopa.pn.model.event.IPnExtChnProgressStatusEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author GIANGR40
 */
@RestController
public class EventApiController {

    @PostMapping("/pecevent")
    IPnExtChnPecEvent newPecEvent(@RequestBody IPnExtChnPecEvent evt) {
        return evt;
    }

    @PostMapping("/progressstatusevent")
    IPnExtChnProgressStatusEvent newPecEvent(@RequestBody IPnExtChnProgressStatusEvent evt) {
        return evt;
    }

}
