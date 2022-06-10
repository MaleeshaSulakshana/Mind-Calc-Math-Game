package com.mind.mind_calc.Question;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class GenerateMaths {

//    Method for create random numbers
    public String numbers(int min, int max) {
        int random = ThreadLocalRandom.current().nextInt(min, max);
        return String.valueOf(random);
    }

//    Method for select random condition
    public String condition() {
        String[] values={"+", "-", "*"};

        Random randomSelector=new Random();
        int randomNumber=randomSelector.nextInt(values.length);

        return values[randomNumber];
    }

}