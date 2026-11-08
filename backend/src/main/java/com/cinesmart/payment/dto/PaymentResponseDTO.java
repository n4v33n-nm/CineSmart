package com.cinesmart.payment.dto;

import com.cinesmart.payment.entity.PaymentMethod;
import com.cinesmart.payment.entity.PaymentStatus;
import com.cinesmart.payment.entity.PaymentTransaction;
import com.cinesmart.ticket.dto.TicketDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PaymentResponseDTO {

    private Long id;
    private String transactionReference;
    private Long bookingId;
    private String bookingReference;
    private Long userId;
    private String userEmail;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String gatewayTxnId;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TicketDTO> tickets;

    public PaymentResponseDTO() {
    }

    public static PaymentResponseDTO fromEntity(PaymentTransaction txn) {
        if (txn == null) return null;
        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.setId(txn.getId());
        dto.setTransactionReference(txn.getTransactionReference());
        dto.setAmount(txn.getAmount());
        dto.setCurrency(txn.getCurrency());
        dto.setPaymentMethod(txn.getPaymentMethod());
        dto.setStatus(txn.getStatus());
        dto.setGatewayTxnId(txn.getGatewayTxnId());
        dto.setFailureReason(txn.getFailureReason());
        dto.setCreatedAt(txn.getCreatedAt());
        dto.setUpdatedAt(txn.getUpdatedAt());

        if (txn.getBooking() != null) {
            dto.setBookingId(txn.getBooking().getId());
            dto.setBookingReference(txn.getBooking().getBookingReference());
        }

        if (txn.getUser() != null) {
            dto.setUserId(txn.getUser().getId());
            dto.setUserEmail(txn.getUser().getEmail());
        }

        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getGatewayTxnId() {
        return gatewayTxnId;
    }

    public void setGatewayTxnId(String gatewayTxnId) {
        this.gatewayTxnId = gatewayTxnId;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<TicketDTO> getTickets() {
        return tickets;
    }

    public void setTickets(List<TicketDTO> tickets) {
        this.tickets = tickets;
    }
}
