/*
 * Copyright (c) 2017 Kang Jinoh <jinoh.kang.kr@gmail.com>. All rights reserved.
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package net.iamahuman.identobf.user;

import net.iamahuman.identobf.nodes.Item;
import net.iamahuman.identobf.nodes.ItemResolver;
import net.iamahuman.identobf.nodes.RequestMeta;
import net.iamahuman.identobf.nodes.RequestMetaAdapter;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by luke1337 on 6/12/17.
 */
public class RequestRegistry extends ItemRegistry {

    List<RequestMetaAdapter> adapters = new CopyOnWriteArrayList<>();

    public RequestRegistry(ItemResolver<? extends Item> resolver) {
        super(resolver);
    }

    public void addRequestMetaAdapter(RequestMetaAdapter adapter) {
        adapters.add(adapter);
    }

    public void addRequestMetaAdapters(RequestMetaAdapter... adapters) {
        List<? super RequestMetaAdapter> list = this.adapters;
        if (list instanceof CopyOnWriteArrayList)
            list.addAll(Arrays.asList(adapters));
        else
            Collections.addAll(list, adapters);
    }

    public void addRequestMetaAdapters(Collection<RequestMetaAdapter> adapters) {
        this.adapters.addAll(adapters);
    }

    public Collection<RequestMeta> getRequests() throws ConstraintException {
        Collection<RequestMeta> requests = new ArrayList<>();
        for (Item item : getItems()) {
            RequestMeta.Builder builder = new RequestMeta.Builder();
            for (RequestMetaAdapter adapter : adapters) {
                RequestMeta meta = adapter.getMeta(this, item);
                if (meta != null) {
                    if (meta.item != item)
                        throw new IllegalArgumentException("meta.item != item");
                    builder.apply(meta);
                }
            }
            requests.add(builder.build(item));
        }
        return requests;
    }

}
