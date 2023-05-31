package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.exception.PassengerNotPresentException;
import com.driver.exception.TrainNotPresentException;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    TrainService trainService;
    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;
    @Autowired
    TicketRepository ticketRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db

        //checking train present or not
        Train train = trainRepository.findById(bookTicketEntryDto.getTrainId()).get();

        String route = train.getRoute();

        int boardingStationIndex = route.indexOf(bookTicketEntryDto.getFromStation().toString());
        int destinationStationIndex = route.indexOf(bookTicketEntryDto.getToStation().toString());
        int bookings = 0;

        for(Ticket ticket : train.getBookedTickets()) {

            int startIndexOfTicket = route.indexOf(ticket.getFromStation().toString());
            int endIndexOfTicket = route.indexOf(ticket.getToStation().toString());

            if ((startIndexOfTicket < destinationStationIndex && boardingStationIndex <= startIndexOfTicket) ||
                    (boardingStationIndex < endIndexOfTicket && endIndexOfTicket <= destinationStationIndex) ||
                    (startIndexOfTicket <= boardingStationIndex && destinationStationIndex <= endIndexOfTicket)) {
                bookings += ticket.getPassengersList().size();
            }
        }

        if(train.getNoOfSeats()-bookings < bookTicketEntryDto.getNoOfSeats()){
            throw new Exception("Less tickets are available");
        }

        List<Passenger> passengers = new ArrayList<>();
        for(int id : bookTicketEntryDto.getPassengerIds()){
            Optional<Passenger> passengerOptional = passengerRepository.findById(id);
            passengers.add(passengerOptional.get());
        }

        Ticket ticket = new Ticket();
        ticket.setPassengersList(passengers);

        String[] routeArr = route.split(",");
        int startIdx = -1;
        for(int i=0; i<routeArr.length; i++){
            if(routeArr[i].equals(bookTicketEntryDto.getFromStation().toString())){
                startIdx = i;
                break;
            }
        }
        int endIdx = -1;
        for(int i=0; i<routeArr.length; i++){
            if(routeArr[i].equals(bookTicketEntryDto.getToStation().toString())){
                endIdx = i;
                break;
            }
        }

        if(startIdx==-1 || endIdx==-1){
            throw new Exception("Invalid stations");
        }

        //calculating price
        int fare = (endIdx-startIdx)*300;
        ticket.setTotalFare(fare);
        ticket.setTrain(train);
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());

        Passenger bookingPassenger = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();

        bookingPassenger.getBookedTickets().add(ticket);

        ticket.setTrain(train);

        ticket = ticketRepository.save(ticket);

        train.getBookedTickets().add(ticket);
        train = trainRepository.save(train);
        return ticket.getTicketId();
    }

    private int findAvailableSeatInTrain(Train train){
        List<Ticket> ticketList = train.getBookedTickets();
        int seatTaken = 0;
        for(Ticket ticket : ticketList){
            seatTaken += ticket.getPassengersList().size();
        }

        return train.getNoOfSeats() - seatTaken;
    }
}
