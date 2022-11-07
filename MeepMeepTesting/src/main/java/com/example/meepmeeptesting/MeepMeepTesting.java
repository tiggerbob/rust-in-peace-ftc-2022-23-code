package com.example.meepmeeptesting;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.noahbres.meepmeep.MeepMeep;
import com.noahbres.meepmeep.roadrunner.DefaultBotBuilder;
import com.noahbres.meepmeep.roadrunner.entity.RoadRunnerBotEntity;

public class MeepMeepTesting {
    public static void main(String[] args) {
        MeepMeep meepMeep = new MeepMeep(700);

        RoadRunnerBotEntity myBot = new DefaultBotBuilder(meepMeep)
                // Set bot constraints: maxVel, maxAccel, maxAngVel, maxAngAccel, track width
                .setConstraints(60, 60, Math.toRadians(180), Math.toRadians(180), 15)
                .followTrajectorySequence(drive ->
                        drive.trajectorySequenceBuilder(new Pose2d(-35, 61, Math.toRadians(270)))
                                .lineToLinearHeading(new Pose2d(-35, 12, Math.toRadians(270)))
                                .lineToLinearHeading(new Pose2d(-23.5, 12, Math.toRadians(270)))
                                .lineToLinearHeading(new Pose2d(-23.5, 10, Math.toRadians(270)))
                                .lineToLinearHeading(new Pose2d(-23.5, 12, Math.toRadians(270)))
                                .lineToLinearHeading(new Pose2d(-35, 12, Math.toRadians(270)))
                                .lineToLinearHeading(new Pose2d(-35, 30, Math.toRadians(270)))
                                .build()
                );

        meepMeep.setBackground(MeepMeep.Background.FIELD_POWERPLAY_OFFICIAL)
                .setDarkMode(true)
                .setBackgroundAlpha(0.95f)
                .addEntity(myBot)
                .start();
    }
}