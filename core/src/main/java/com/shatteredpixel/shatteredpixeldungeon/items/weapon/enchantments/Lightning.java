/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2022 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Electricity;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.BArray;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;

public class Lightning extends Weapon.Enchantment {

    private static ItemSprite.Glowing WHITE = new ItemSprite.Glowing(0xFFFFFF, 0.5f);

    @Override
    public int proc(Weapon weapon, Char attacker, Char defender, int damage) {
        int pos = defender.pos;
        if (Dungeon.level.heroFOV[pos]){
            Sample.INSTANCE.play( Assets.Sounds.LIGHTNING );
        }

        for( int i : PathFinder.NEIGHBOURS9) {
            if (!Dungeon.level.solid[pos + i]) {
                GameScene.add(Blob.seed(pos + i, 2, Electricity.class));
            }
        }

        affected.clear();
        arcs.clear();

        affected.remove(defender);
        arc(attacker, defender, 5, affected, arcs);

        for (Char ch : affected) {
            if (ch.alignment != attacker.alignment) {
                ch.damage(Math.round(damage), this);
            }
        }

        attacker.sprite.parent.addToFront(new com.shatteredpixel.shatteredpixeldungeon.effects.Lightning(arcs, null));
        Sample.INSTANCE.play(Assets.Sounds.LIGHTNING);

        return damage;

    }

    @Override
    public ItemSprite.Glowing glowing() {
        return WHITE;
    }

    private ArrayList<Char> affected = new ArrayList<>();

    private ArrayList<com.shatteredpixel.shatteredpixeldungeon.effects.Lightning.Arc> arcs = new ArrayList<>();

    public static void arc(Char attacker, Char defender, int dist, ArrayList<Char> affected, ArrayList<com.shatteredpixel.shatteredpixeldungeon.effects.Lightning.Arc> arcs) {

        affected.add(defender);

        defender.sprite.centerEmitter().burst(SparkParticle.FACTORY, 3);
        defender.sprite.flash();

        PathFinder.buildDistanceMap(defender.pos, BArray.not(Dungeon.level.solid, null), dist);
        for (int i = 0; i < PathFinder.distance.length; i++) {
            if (PathFinder.distance[i] < Integer.MAX_VALUE) {
                Char n = Actor.findChar(i);
                if (n != null && n != attacker && !affected.contains(n)) {
                    arcs.add(new com.shatteredpixel.shatteredpixeldungeon.effects.Lightning.Arc(defender.sprite.center(), n.sprite.center()));
                    arc(attacker, n, (Dungeon.level.water[n.pos] && !n.flying) ? 2 : 1, affected, arcs);
                }
            }
        }
    }
}
