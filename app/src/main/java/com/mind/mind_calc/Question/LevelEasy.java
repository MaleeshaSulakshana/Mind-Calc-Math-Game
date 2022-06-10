package com.mind.mind_calc.Question;

public class LevelEasy extends Question {

    @Override
    public String generateQuestion() {
        GenerateMaths generateMaths = new GenerateMaths();

        String[] numbers = new String[2];
        for (int i=0; i< 2; i++) {
            numbers[i] = generateMaths.numbers(01, 99);
        }

        String condition = generateMaths.condition();
        String question = numbers[0]+" "+condition+" "+numbers[1];

        return question;
    }

}
