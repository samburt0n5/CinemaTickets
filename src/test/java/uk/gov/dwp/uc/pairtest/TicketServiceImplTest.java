package uk.gov.dwp.uc.pairtest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

@RunWith(MockitoJUnitRunner.class)
public class TicketServiceImplTest {
    
    @InjectMocks
    TicketService ticketService = new TicketServiceImpl();
    @Mock
    TicketPaymentService ticketPaymentService;
    @Mock
    SeatReservationService seatReservationService;
    
    @Before
    public void setUp() throws Exception {
    }
    
    @Test
    public void testPurchaseTickets() {
        TicketTypeRequest ticketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[]{ticketTypeRequest};
        ticketService.purchaseTickets(1L, ticketTypeRequests);
        Mockito.verify(ticketPaymentService, Mockito.times(1)).makePayment(Mockito.anyLong(), Mockito.anyInt());
        Mockito.verify(seatReservationService, Mockito.times(1)).reserveSeat(Mockito.anyLong(), Mockito.anyInt());
    }
    
    @Test
    public void testPurchaseTickets1Adult() {
        TicketTypeRequest ticketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[]{ticketTypeRequest};
        ticketService.purchaseTickets(1L, ticketTypeRequests);
        int expectedTotal = TicketTypeRequest.Type.ADULT.getPrice();
        Mockito.verify(ticketPaymentService, Mockito.times(1)).makePayment(1L, expectedTotal);
        Mockito.verify(seatReservationService, Mockito.times(1)).reserveSeat(1L, 1);
    }
    
    @Test
    public void testPurchaseTickets1AdultAnd1Child() {
        TicketTypeRequest ticketTypeRequest1 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        TicketTypeRequest ticketTypeRequest2 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);
        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[]{ticketTypeRequest1, ticketTypeRequest2};
        ticketService.purchaseTickets(1L, ticketTypeRequests);
        int expectedTotal = TicketTypeRequest.Type.ADULT.getPrice() + TicketTypeRequest.Type.CHILD.getPrice();
        Mockito.verify(ticketPaymentService, Mockito.times(1)).makePayment(1L, expectedTotal);
        Mockito.verify(seatReservationService, Mockito.times(1)).reserveSeat(1L, 2);
    }
    
    @Test
    public void testPurchaseTickets3AdultAnd2ChildAnd1Infant() {
        TicketTypeRequest ticketTypeRequest1 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 3);
        TicketTypeRequest ticketTypeRequest2 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);
        TicketTypeRequest ticketTypeRequest3 = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);
        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[]{ticketTypeRequest1, ticketTypeRequest2, ticketTypeRequest3};
        ticketService.purchaseTickets(1L, ticketTypeRequests);
        int expectedTotal = (TicketTypeRequest.Type.ADULT.getPrice()*3) + (TicketTypeRequest.Type.CHILD.getPrice()*2) + TicketTypeRequest.Type.INFANT.getPrice();
        Mockito.verify(ticketPaymentService, Mockito.times(1)).makePayment(1L, expectedTotal);
        Mockito.verify(seatReservationService, Mockito.times(1)).reserveSeat(1L, 5);
    }
    
    @Test(expected = InvalidPurchaseException.class)
    public void testPurchaseTicketsChildWithNoAdult() {
        TicketTypeRequest ticketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);
        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[]{ticketTypeRequest};
        ticketService.purchaseTickets(1L, ticketTypeRequests);
        Mockito.verify(ticketPaymentService, Mockito.times(0)).makePayment(Mockito.any(), Mockito.any());
        Mockito.verify(seatReservationService, Mockito.times(0)).reserveSeat(Mockito.any(), Mockito.any());
    }
    
    @Test(expected = InvalidPurchaseException.class)
    public void testPurchaseTicketsInfantWithNoAdult() {
        TicketTypeRequest ticketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);
        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[]{ticketTypeRequest};
        ticketService.purchaseTickets(1L, ticketTypeRequests);
        Mockito.verify(ticketPaymentService, Mockito.times(0)).makePayment(Mockito.any(), Mockito.any());
        Mockito.verify(seatReservationService, Mockito.times(0)).reserveSeat(Mockito.any(), Mockito.any());
    }
    
    @Test(expected = InvalidPurchaseException.class)
    public void testPurchaseMoreThan20Tickets() {
        TicketTypeRequest ticketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 20);
        TicketTypeRequest ticketTypeRequest2 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[]{ticketTypeRequest,ticketTypeRequest2};
        ticketService.purchaseTickets(1L, ticketTypeRequests);
        Mockito.verify(ticketPaymentService, Mockito.never()).makePayment(Mockito.any(), Mockito.any());
        Mockito.verify(seatReservationService, Mockito.never()).reserveSeat(Mockito.any(), Mockito.any());
    }
    
    @Test(expected = InvalidPurchaseException.class)
    public void testPurchase0Tickets() {
        TicketTypeRequest ticketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 0);
        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[]{ticketTypeRequest};
        ticketService.purchaseTickets(1L, ticketTypeRequests);
        Mockito.verify(ticketPaymentService, Mockito.times(0)).makePayment(Mockito.any(), Mockito.any());
        Mockito.verify(seatReservationService, Mockito.times(0)).reserveSeat(Mockito.any(), Mockito.any());
    }
    
    @Test(expected = InvalidPurchaseException.class)
    public void testPurchaseTicketsInvalidAccountId() {
        TicketTypeRequest ticketTypeRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[]{ticketTypeRequest};
        ticketService.purchaseTickets(0L, ticketTypeRequests);
        Mockito.verify(ticketPaymentService, Mockito.times(0)).makePayment(Mockito.any(), Mockito.any());
        Mockito.verify(seatReservationService, Mockito.times(0)).reserveSeat(Mockito.any(), Mockito.any());
    }
    
    
}