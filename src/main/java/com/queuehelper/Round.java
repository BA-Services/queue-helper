package com.queuehelper;


/*
 * Copyright (c) 2018, Cameron <https://github.com/noremac201>
 * Copyright (c) 2018, Jacob M <https://github.com/jacoblairm>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.time.Duration;
import java.time.Instant;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import static net.runelite.client.util.RSTimeUnit.GAME_TICKS;

class Round
{
    private final Instant roundStartTime;
    @Getter
    private final Role roundRole;
    @Getter
    @Setter
    private boolean runnersKilled;
    @Getter
    @Setter
    private boolean rangersKilled;
    @Getter
    @Setter
    private boolean healersKilled;
    @Getter
    @Setter
    private boolean fightersKilled;

    @Inject
    public Round(Role role)
    {
        this.roundRole = role;
        this.roundStartTime = Instant.now().plus(Duration.of(2, GAME_TICKS));
    }

    public long getRoundTime()
    {
        return Duration.between(roundStartTime, Instant.now()).getSeconds();
    }

    public long getTimeToChange()
    {
        return 30 + (Duration.between(Instant.now(), roundStartTime).getSeconds() % 30);
    }
}

