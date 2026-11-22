package com.ederproducer.ankiingles.mobile.model

/** As 30 frases iniciais do webapp, para começar sem cartões vazios. */
object SeedDeck {
    private val PHRASES = listOf(
        "How's it going?" to "Como vai? Tudo bem?",
        "I am running a little late." to "Estou um pouco atrasado.",
        "Could you give me a hand with this?" to "Você pode me dar uma mão com isso?",
        "Let's grab a coffee sometime." to "Vamos tomar um café alguma hora.",
        "That's not what I meant." to "Não foi isso que eu quis dizer.",
        "Do you mind if I open the window?" to "Você se importa se eu abrir a janela?",
        "It's been a long day." to "Foi um dia longo.",
        "I'll get back to you on that." to "Eu te respondo sobre isso depois.",
        "Sounds good to me." to "Parece bom para mim.",
        "I'm not sure yet." to "Ainda não tenho certeza.",
        "Could you say that again, please?" to "Você pode repetir isso, por favor?",
        "I'm looking forward to it." to "Estou ansioso por isso.",
        "No worries." to "Sem problemas.",
        "Let me think about it." to "Deixa eu pensar sobre isso.",
        "I'm on my way." to "Estou a caminho.",
        "It doesn't make sense to me." to "Não faz sentido para mim.",
        "Would you like something to drink?" to "Você gostaria de algo para beber?",
        "I used to live here." to "Eu costumava morar aqui.",
        "I'm getting used to it." to "Estou me acostumando com isso.",
        "By the way, I saw Anna yesterday." to "Aliás, eu vi a Anna ontem.",
        "I can't wait to see you." to "Mal posso esperar para te ver.",
        "That reminds me of a story." to "Isso me lembra uma história.",
        "Let's call it a day." to "Vamos encerrar por hoje.",
        "I'm in." to "Eu topo. / Conta comigo.",
        "It's up to you." to "Depende de você.",
        "I had no idea." to "Eu não tinha ideia.",
        "Thanks for letting me know." to "Obrigado por me avisar.",
        "I'm gonna head out now." to "Vou sair agora.",
        "Do you have a minute?" to "Você tem um minuto?",
        "I appreciate your help." to "Eu agradeço a sua ajuda."
    )

    fun create(): MutableList<Card> = PHRASES.mapIndexed { index, (front, back) ->
        Srs.newCard("seed-$index", front, back)
    }.toMutableList()
}
