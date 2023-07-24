package com.tenpo.challenge.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class ExternalService {
    //Aca deberia usar un mock server a algun localhost:xxxx
    public double getPercentage() {
        // Devuelve un porcentaje aleatorio entre 1 y 100
        return 1 + new Random().nextDouble() * (100 - 1);
    }
}
