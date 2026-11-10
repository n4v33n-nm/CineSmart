package com.cinesmart.ticket.dto;

import java.util.List;

public class TicketStatsDTO {

    private long totalGenerated;
    private long verifiedTickets;
    private long unusedTickets;
    private long invalidAttempts;
    private List<TicketVerificationLogDTO> recentVerifications;

    public TicketStatsDTO() {}

    public TicketStatsDTO(long totalGenerated, long verifiedTickets, long unusedTickets, long invalidAttempts, List<TicketVerificationLogDTO> recentVerifications) {
        this.totalGenerated = totalGenerated;
        this.verifiedTickets = verifiedTickets;
        this.unusedTickets = unusedTickets;
        this.invalidAttempts = invalidAttempts;
        this.recentVerifications = recentVerifications;
    }

    public long getTotalGenerated() {
        return totalGenerated;
    }

    public void setTotalGenerated(long totalGenerated) {
        this.totalGenerated = totalGenerated;
    }

    public long getVerifiedTickets() {
        return verifiedTickets;
    }

    public void setVerifiedTickets(long verifiedTickets) {
        this.verifiedTickets = verifiedTickets;
    }

    public long getUnusedTickets() {
        return unusedTickets;
    }

    public void setUnusedTickets(long unusedTickets) {
        this.unusedTickets = unusedTickets;
    }

    public long getInvalidAttempts() {
        return invalidAttempts;
    }

    public void setInvalidAttempts(long invalidAttempts) {
        this.invalidAttempts = invalidAttempts;
    }

    public List<TicketVerificationLogDTO> getRecentVerifications() {
        return recentVerifications;
    }

    public void setRecentVerifications(List<TicketVerificationLogDTO> recentVerifications) {
        this.recentVerifications = recentVerifications;
    }
}

