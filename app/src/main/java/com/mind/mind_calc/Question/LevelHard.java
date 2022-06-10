package com.mind.mind_calc.Question;

public class LevelHard extends Question {

    @Override
    public String generateQuestion() {
        GenerateMaths generateMaths = new GenerateMaths();

        String[] numbers = new String[4];
        for (int i=0; i< 4; i++) {
            numbers[i] = generateMaths.numbers(01, 99);
        }

        String[] conditions = new String[3];
        for (int i=0; i< 3; i++) {
            conditions[i] = generateMaths.condition();
        }

        String question = numbers[0]+" "+
                conditions[0]+" "+
                numbers[1]+" "+
                conditions[1]+" "+
                numbers[2]+" "+
                conditions[2]+" "+
                numbers[3];

        return question;
    }
}