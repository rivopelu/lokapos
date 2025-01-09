package com.lokapos.services;

import com.lokapos.annotations.BaseController;
import com.lokapos.entities.ServingOrder;
import com.lokapos.model.request.ReqNotificationMidTrans;
import com.lokapos.model.request.ReqPaymentObject;
import com.lokapos.model.response.SnapPaymentResponse;

@BaseController
public interface PaymentService {
    SnapPaymentResponse createPayment(ReqPaymentObject req);

    String postNotificationFromMidTrans(ReqNotificationMidTrans req);

    String createPaymentCustomInterface(ReqPaymentObject reqPaymentObject);

    String createPaymentUsingEWallet(ServingOrder order);
}
