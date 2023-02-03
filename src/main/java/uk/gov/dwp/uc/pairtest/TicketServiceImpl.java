package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;


public class TicketServiceImpl implements TicketService {
    
    private TicketPaymentService ticketPaymentService;
    private SeatReservationService seatReservationService;

    /**
     * Should only have private methods other than the one below.
     */
    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        validateAccountId(accountId);
        //Some method calls here to calculate amount to pay/how many seats required
        int amountToPay;
        int totalNumberOfSeatsToReserve=0;
        totalNumberOfSeatsToReserve = calculateTotalNumberOfSeats(ticketTypeRequests);
        amountToPay=calculateAmountToPay(ticketTypeRequests);
        
        //Making calls to the TicketPaymentService & SeatReservationService to actully book
        ticketPaymentService.makePayment(accountId, amountToPay);
        seatReservationService.reserveSeat(accountId, totalNumberOfSeatsToReserve);
    }
    
    private static void validateAccountId(Long accountId) {
        if(!(accountId > 0)){
            throw new InvalidPurchaseException();
        }
    }
    
    private int calculateTotalNumberOfSeats(TicketTypeRequest[] ticketTypeRequests) {
        AtomicInteger amountOfSeatsToAllocate = new AtomicInteger();
        boolean containsChildOrInfant = isChildOrInfantInRequests(ticketTypeRequests);
        boolean containsAdult = Arrays.stream(ticketTypeRequests).map(TicketTypeRequest::getTicketType).
                anyMatch(TicketTypeRequest.Type.ADULT::equals);
        if(containsChildOrInfant && !containsAdult){
            throw new InvalidPurchaseException();
        }
        AtomicInteger totalNumberOfTickets= new AtomicInteger();
        Arrays.stream(ticketTypeRequests).forEach(ticketTypeRequest -> totalNumberOfTickets.addAndGet(ticketTypeRequest.getNoOfTickets()));
        if(totalNumberOfTickets.get()>20 || totalNumberOfTickets.get()<1){
            throw new InvalidPurchaseException();
        }
        Arrays.stream(ticketTypeRequests).forEach(ticketTypeRequest -> {
            if(!TicketTypeRequest.Type.INFANT.equals(ticketTypeRequest.getTicketType())){
                amountOfSeatsToAllocate.getAndAdd(ticketTypeRequest.getNoOfTickets());
            }
        });
        return amountOfSeatsToAllocate.get();
    }
    
    private boolean isChildOrInfantInRequests(TicketTypeRequest[] ticketTypeRequests) {
        for (TicketTypeRequest ticketTypeRequest : ticketTypeRequests) {
            if (TicketTypeRequest.Type.CHILD.equals(ticketTypeRequest.getTicketType()) ||
                    TicketTypeRequest.Type.INFANT.equals(ticketTypeRequest.getTicketType())) {
                return true;
            }
        }
        return false;
    }
    
    private int calculateAmountToPay(TicketTypeRequest[] ticketTypeRequests) {
        AtomicInteger amount= new AtomicInteger();
        Arrays.stream(ticketTypeRequests).forEach(ticketTypeRequest -> {
            if(TicketTypeRequest.Type.ADULT.equals(ticketTypeRequest.getTicketType())){
                amount.getAndAdd((TicketTypeRequest.Type.ADULT.getPrice())*ticketTypeRequest.getNoOfTickets());
            }
            if(TicketTypeRequest.Type.CHILD.equals(ticketTypeRequest.getTicketType())){
                amount.getAndAdd((TicketTypeRequest.Type.CHILD.getPrice())*ticketTypeRequest.getNoOfTickets());
            }
        });
        return amount.get();
    }
}
