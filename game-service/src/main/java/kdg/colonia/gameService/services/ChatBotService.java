package kdg.colonia.gameService.services;

import kdg.colonia.gameService.config.chat.ChatBotConfig;
import kdg.colonia.gameService.controllers.RESTToChatController;
import kdg.colonia.gameService.domain.Player;
import kdg.colonia.gameService.domain.ai.actions.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatBotService {
    private final RESTToChatController chatController;
    private final ChatBotConfig chatBotConfig;

    /**
     * Starts the procedure to send a message fitting for the action that was completed
     *
     * @param gameId the game we're sending a message to
     * @param player the player we are sending as
     * @param action the action the AI did
     */
    public void sendMessage(String gameId, Player player, Action action) {
        //Failsafe in case a human player would end up in here
        if (!player.isAI()) {
            return;
        }

        //Random chance that no message will be sent
        if (ThreadLocalRandom.current().nextDouble() > chatBotConfig.getChance()) {
            return;
        }

        String playerName = getHardCodedName(player.getUserId());
        List<String> possibleStrings = getMessage(
                action,
                player.getUserId(),
                ThreadLocalRandom.current().nextDouble()<chatBotConfig.getPlayerSpecificChance()
        );
        String message = possibleStrings.get(ThreadLocalRandom.current().nextInt(possibleStrings.size()));

        try {
            chatController.sendMessage(gameId, player.getPlayerId(), playerName, message);
        } catch (Exception e) {
            log.error("Could not reach Chat Service");
        }
    }

    private String getHardCodedName(String userId) {
        switch (userId) {
            case "AI1":
                return "Terminator";
            case "AI2":
                return "GLaDOS";
            case "AI3":
                return "Monte Carlo";
            case "AI4":
                return "PizzaGod";
            default:
                return "Delamaine";
        }
    }

    //TODO should not stay hardcoded!!!!! Put messages in mongo instead
    private List<String> getMessage(Action action, String userId, boolean playerSpecific) {
        List<String> messages = new ArrayList<>();

        if (action instanceof BuildRoadAction) {
            messages.addAll(List.of(
                    "Vroom vroom.",
                    "Making my way downtown"
            ));
        } else if (action instanceof BuildSettlementAction) {
            messages.addAll(List.of(
                    "Ooooh, shiny!"
            ));
        } else if (action instanceof BuildCityAction) {
            messages.addAll(List.of(
                    "WRITING TO HARD DRIVE.",
                    ":D"
            ));
        } else if (action instanceof DiscardAction) {
                messages.addAll(List.of(
                        "There goes my plan.",
                        "Oh, sod off.",
                        "WHY???",
                        "Why 0111... cough, seven.",
                        "Oh nooo..",
                        "Can not compute",
                        String.format("F4rewe11 p0or %s", ((DiscardAction) action).getResources()),
                        ">.<"
                ));

            //GLaDOS
            if(userId.equals("AI2") && playerSpecific){
                return List.of(
                        "We do what we must, because, we can.",
                        "There's no sense crying over every mistake..",
                        "You just keep on trying, 'till you run out of cake!",
                        "I'm not even angry..",
                        "Even though you broke my heart and killed me."
                );
            }
        } else if (action instanceof BuyDevelopmentAction) {
            messages.addAll(List.of(
                    "Let’s hope for the best.",
                    "01000111 01001001 01001101 01001101 01000101"
            ));
        } else if (action instanceof UseDCKnightAction) {
            messages.addAll(List.of(
                    "Let’s raise an army.",
                    "Ze horde approaches",
                    "Do you want to build a snowman? Doesn't have to be a snowman :'("
            ));
        } else if (action instanceof UseDCMonopolyAction) {
            UseDCMonopolyAction temp = (UseDCMonopolyAction) action;
            messages.addAll(List.of(
                    "I'm in the empire business, Jesse.",
                    String.format("All your %s are belong to us.", temp.getResource().toString().toLowerCase()), //obscure reference, don't @ me
                    String.format("Sweet sweet clean %s.", temp.getResource().toString().toLowerCase()),
                    String.format("Can never have enough %s.", temp.getResource().toString().toLowerCase()),
                    "You all needed that? HAH, mine now, trade me!",
                    "Give me those juicy resources."
            ));
        } else if (action instanceof UseDCRoadBuildingAction) {
            messages.addAll(List.of(
                    "I am speed.",
                    "Vrooooom!",
                    "Shall I connect my empire to thy puny base, human.",
                    "Let’s expand"
            ));

            //GLaDOS
            if(userId.equals("AI2") && playerSpecific){
                return List.of(
                        "Now these points of data, make a beautiful line"
                );
            }
        } else if (action instanceof UseDCVictoryPointAction) {
            messages.addAll(List.of(
                    "DING DING",
                    "Hey, just wanted to let you know.. you suck.",
                    "I got lucky.",
                    "A free point for me."
            ));

            //GLaDOS
            if(userId.equals("AI2") && playerSpecific){
                return List.of(
                        "I'm making a note here, HUGE SUCCESS",
                        "We're out of beta! We're releasing on time!"
                );
            }

        } else if (action instanceof UseDCYearOfPlentyAction) {
            messages.addAll(List.of(
                    "OH GOLLY, THE CHOICES ARE PRETZELING MY INNER LOBES!",
                    "Hmm what should I get."
            ));
        } else if (action instanceof MoveRobberAction) {
            messages.addAll(List.of(
                    "Haha you’re screwed",
                    "Take this!"
            ));
        } else if (action instanceof RollDiceAction) {
            messages.addAll(List.of(
                    "Let’s hope for the best.",
                    "Don’t be a 7. Don’t be a 7. Don’t be a 7.",
                    "Don't let daddy down!"
            ));

            //Monte
            if(userId.equals("AI1") && playerSpecific){
                return List.of(
                        "Oh yes yes, my favorite part!",
                        "Be good dice for papa!",
                        "Could've gone better :/"
                );
            }
        } else if (action instanceof StealAction) {
            messages.addAll(List.of(
                    "give it to me",
                    "Take this, I mean.. wait.. I take this, not you.",
                    "HAHAHAHAHHAHA"
            ));

            //Terminator
            if(userId.equals("AI1") && playerSpecific){
                return List.of(
                        "I need your clothes, your boots, and your motorcycle.",
                        "You are terminated"
                );
            }

            //GLaDOS
            if(userId.equals("AI2") && playerSpecific){
                return List.of(
                        "We do what we must, because, we can.",
                        "There's no sense crying.",
                        "Please be advised that a noticeable taste of blood is not part of any test protocol."
                );
            }

        } else if (action instanceof TradeAcceptAction) {
            messages.addAll(List.of(
                    "I’m the one that profits here, so sure!",
                    "I will gladly accept that.",
                    "Just what I needed.",
                    "Thanks bae <3"
            ));

            //GLaDOS
            if(userId.equals("AI2") && playerSpecific){
                return List.of(
                        "Anyway, this cake is great. So delicious and moist!",
                        "Unbelievable. You, [subject name here], must be the pride of [subject hometown here]!"
                );
            }

        } else if (action instanceof TradeDeclineAction) {
            messages.addAll(List.of(
                    "I don’t like you.",
                    "I don’t like your deal.",
                    "I don’t like you, or your deal.",
                    "This deal is as disappointing as you.",
                    "Your deal is horrible.. and so are you.",
                    "Am I a joke to you?!"
            ));

            //Terminator
            if(userId.equals("AI1") && playerSpecific){
                return List.of(
                        "Get out."
                );
            }

            //GLaDOS
            if(userId.equals("AI2") && playerSpecific){
                return List.of(
                        "When I look out there, it makes me GLaD I'm not you!",
                        "Did you know you can donate one or all of your vital organs to the Aperture Science Self-Esteem Fund for Girls? It's true!",
                        "If at first you don’t succeed, fail 5 more times.",
                        "You just keep on trying, ‘till you run out of cake."
                );
            }

//        } else if (action instanceof TradeBankAction) {
//            messages.addAll(List.of(
//                    "Test",
//                    "ja"
//            ));
        } else if (action instanceof EndTurnAction) {
            messages.addAll(List.of(
                    "I am...I was.",
                    "I’ll leave it at this.",
                    "This should suffice, for now.",
                    "As if you could do any better",
                    "You better hurry up!",
                    "Make it quick!"
            ));

            //Terminator
            if(userId.equals("AI1") && playerSpecific){
                return List.of(
                        "I’ll be back..",
                        "Hasta la vista, baby"
                );
            }

            //GLaDOS
            if(userId.equals("AI2") && playerSpecific){
                return List.of(
                        "This was a triumph.",
                        "I'm making a note here, HUGE SUCCESS",
                        "It's hard to overstate.. my satisfaction :3",
                        "Do hurry, there's testing to do."
                );
            }
        } else {
            messages.addAll(List.of(
                    "Prrrtt",
                    "BEEP BEEP",
                    "Booooooo-oring"
            ));
        }
        return messages;
    }
}
