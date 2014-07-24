/**
 * This file is part of jetson.
 *
 * jetson is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jetson is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jetson.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.standrews.cs.trombone.core.rpc;

import java.lang.reflect.Method;
import java.util.ArrayList;
import org.mashti.jetson.ServerChannelInitializer;
import org.mashti.jetson.lean.LeanResponseEncoder;
import org.mashti.jetson.lean.codec.Codecs;
import org.mashti.jetson.util.ReflectionUtil;

/** @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk) */
public class LeanPeerServerChannelInitializer<Service> extends ServerChannelInitializer {

    public LeanPeerServerChannelInitializer(final Class<Service> service_type, final Codecs codecs) {

        super(new LeanPeerRequestDecoder(new ArrayList<Method>(ReflectionUtil.mapMethodsToNames(service_type).keySet()), codecs), new LeanResponseEncoder(codecs));
    }
}
