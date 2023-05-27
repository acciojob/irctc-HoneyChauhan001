package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
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
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


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
        List<Passenger> passengers = new ArrayList<>();
        //check passegers in list present or not
        for(int id : bookTicketEntryDto.getPassengerIds()){
            Optional<Passenger> passengerOptional = passengerRepository.findById(id);
            if(!passengerOptional.isPresent()){
                throw new PassengerNotPresentException("passenger with " + id + "not present");
            }
            passengers.add(passengerOptional.get());
        }

        //check booking person present or not
        Optional<Passenger> bookingPassengerOpt = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId());
        if(!bookingPassengerOpt.isPresent()){
            throw new Exception("booking passenger not present");
        }
        Passenger bookingPassenger = bookingPassengerOpt.get();

        //checking train present or not
        Optional<Train> trainOpt = trainRepository.findById(bookTicketEntryDto.getTrainId());
        if(!trainOpt.isPresent()){
            throw new TrainNotPresentException("Train not present with this id");
        }
        Train train = trainOpt.get();

        //checking availability of seats
        int seatAvailable = findAvailableSeatInTrain(train);
        if(seatAvailable<bookTicketEntryDto.getNoOfSeats()){
            throw new Exception("booking passenger not present");
        }

        //checking stations and sequence
        String route = train.getRoute();
        String[] routeArray = route.split(",");
        int idxOfFromStation = -1;
        int idxOfToStation = -1;

        for(int i=0; i<routeArray.length;i++){
            if(routeArray[i].equals(String.valueOf(bookTicketEntryDto.getFromStation())))idxOfFromStation=i;
            if(routeArray[i].equals(String.valueOf(bookTicketEntryDto.getToStation())))idxOfToStation=i;
        }
        if(idxOfFromStation==-1 || idxOfToStation == -1 || idxOfToStation<=idxOfFromStation){
            throw new Exception("booking passenger not present");
        }
        //calculating price
        int fare = (idxOfToStation-idxOfFromStation)*300;
        int totalFare = fare*bookTicketEntryDto.getNoOfSeats();



        Ticket ticket = new Ticket();
        ticket.setPassengersList(passengers);
        ticket.setTotalFare(totalFare);
        ticket.setTrain(train);
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());

        train.getBookedTickets().add(ticket);

        Train savedTrain = trainRepository.save(train);

        int size = savedTrain.getBookedTickets().size();
        Ticket savedTicket = savedTrain.getBookedTickets().get(size-1);

//        bookingPassenger.getBookedTickets().add(savedTicket);
//        for(Passenger passenger : passengers){
//            passenger.getBookedTickets().add(savedTicket);
//        }

       return savedTicket.getTicketId();

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
