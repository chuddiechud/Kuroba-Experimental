/*
 * KurobaEx - *chan browser https://github.com/K1rakishou/Kuroba-Experimental/
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
 */
package com.github.adamantcheese.chan.ui.toolbar;

import android.graphics.drawable.Drawable;
import android.view.View;

import com.github.adamantcheese.chan.R;
import com.github.adamantcheese.chan.controller.NavigationController;

import java.util.ArrayList;
import java.util.List;

import static com.github.adamantcheese.chan.utils.AndroidUtils.getString;

/**
 * The navigation properties for a Controller. Controls common properties that parent controllers
 * need to know, such as the title of the controller.
 * <p>
 * This is also used to set up the toolbar menu, see {@link #buildMenu()}}.
 */
public class NavigationItem {
    public String title = "";
    public String subtitle = "";

    public boolean hasBack = true;
    public boolean hasDrawer;
    public boolean handlesToolbarInset;
    public boolean swipeable = true;

    public String searchText;
    public boolean search;

    protected ToolbarMenu menu;
    protected ToolbarMiddleMenu middleMenu;
    protected View rightView;

    public boolean hasArrow() {
        return hasBack || search;
    }

    public void setTitle(int resId) {
        title = getString(resId);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public MenuBuilder buildMenu() {
        return new MenuBuilder(this);
    }

    public void setMiddleMenu(ToolbarMiddleMenu middleMenu) {
        this.middleMenu = middleMenu;
    }

    public void setRightView(View view) {
        rightView = view;
    }

    public ToolbarMenuItem findItem(int id) {
        return menu == null ? null : menu.findItem(id);
    }

    public ToolbarMenuSubItem findSubItem(int id) {
        return menu == null ? null : menu.findSubItem(id);
    }

    public static class MenuBuilder {
        private final NavigationItem navigationItem;
        private final ToolbarMenu menu;

        public MenuBuilder(NavigationItem navigationItem) {
            this.navigationItem = navigationItem;
            menu = new ToolbarMenu();
        }

        public MenuBuilder withItem(int drawable, ToolbarMenuItem.ClickCallback clicked) {
            return withItem(-1, drawable, clicked);
        }

        public MenuBuilder withItem(int id, int drawable, ToolbarMenuItem.ClickCallback clicked) {
            return withItem(new ToolbarMenuItem(id, drawable, clicked));
        }

        public MenuBuilder withItem(int id, Drawable drawable, ToolbarMenuItem.ClickCallback clicked) {
            return withItem(new ToolbarMenuItem(id, drawable, clicked));
        }

        public MenuBuilder withItem(ToolbarMenuItem menuItem) {
            menu.addItem(menuItem);
            return this;
        }

        public MenuOverflowBuilder withOverflow(NavigationController navigationController) {
            return new MenuOverflowBuilder(this,
                    new ToolbarMenuItem(
                            ToolbarMenu.OVERFLOW_ID,
                            R.drawable.ic_more_vert_white_24dp,
                            ToolbarMenuItem::showSubmenu,
                            navigationController,
                            null
                    )
            );
        }

        public MenuOverflowBuilder withOverflow(
                NavigationController navigationController,
                ToolbarMenuItem.ToobarThreedotMenuCallback threedotMenuCallback
        ) {
            return new MenuOverflowBuilder(this,
                    new ToolbarMenuItem(
                            ToolbarMenu.OVERFLOW_ID,
                            R.drawable.ic_more_vert_white_24dp,
                            ToolbarMenuItem::showSubmenu,
                            navigationController,
                            threedotMenuCallback
                    ));
        }

        public ToolbarMenu build() {
            navigationItem.menu = menu;
            return menu;
        }
    }

    public static class MenuOverflowBuilder {
        private final MenuBuilder menuBuilder;
        private final ToolbarMenuItem menuItem;

        public MenuOverflowBuilder(MenuBuilder menuBuilder, ToolbarMenuItem menuItem) {
            this.menuBuilder = menuBuilder;
            this.menuItem = menuItem;
        }

        public MenuOverflowBuilder withSubItem(
                int id,
                int text,
                ToolbarMenuSubItem.ClickCallback clicked
        ) {
            return withSubItem(id, getString(text), true, clicked);
        }

        public MenuOverflowBuilder withSubItem(
                int id,
                int text,
                boolean enabled,
                ToolbarMenuSubItem.ClickCallback clicked
        ) {
            return withSubItem(id, getString(text), enabled, clicked);
        }

        public MenuOverflowBuilder withSubItem(
                int id,
                String text,
                boolean enabled,
                ToolbarMenuSubItem.ClickCallback clicked
        ) {
            menuItem.addSubItem(new ToolbarMenuSubItem(id, text, enabled, clicked));

            return this;
        }

        /**
         * Note: this method only supports one level of depth. If you need more you will have to
         * implement it yourself. The reason for that is that at the time of writing this there
         * was no need for more than one level of depth.
         * @see ToolbarMenuItem#showSubmenu()
         *
         * Note2: all menu ids have to be unique. MenuItems without id at all (-1) are not allowed too.
         * Otherwise this will crash in
         * @see ToolbarMenuItem#showSubmenu()
         * */
        public MenuNestedOverflowBuilder withNestedOverflow(
                int id,
                int textId,
                boolean enabled
        ) {
            return new MenuNestedOverflowBuilder(this,
                    new ToolbarMenuSubItem(
                            id,
                            textId,
                            enabled
                    ));
        }

        public MenuOverflowBuilder addNestedItemsTo(
                int ownerMenuItem,
                List<ToolbarMenuSubItem> nestedMenuItems
        ) {
            for (ToolbarMenuSubItem subItem : menuItem.subItems) {
                if (subItem.id == ownerMenuItem) {
                    for (ToolbarMenuSubItem nestedItem : nestedMenuItems) {
                        subItem.addNestedItem(nestedItem);
                    }

                    break;
                }
            }


            return this;
        }

        public MenuBuilder build() {
            return menuBuilder.withItem(menuItem);
        }
    }

    public static class MenuNestedOverflowBuilder {
        private final MenuOverflowBuilder menuOverflowBuilder;
        private final ToolbarMenuSubItem menuSubItem;
        private final List<ToolbarMenuSubItem> nestedMenuItems = new ArrayList<>();

        public MenuNestedOverflowBuilder(
                MenuOverflowBuilder menuOverflowBuilder,
                ToolbarMenuSubItem menuSubItem
        ) {
            this.menuOverflowBuilder = menuOverflowBuilder;
            this.menuSubItem = menuSubItem;
        }

        public MenuNestedOverflowBuilder addNestedItem(
                int itemId,
                int text,
                boolean enabled,
                boolean isCurrentlySelected,
                Object value,
                ToolbarMenuSubItem.ClickCallback clickCallback
        ) {
            for (ToolbarMenuSubItem subItem : menuSubItem.moreItems) {
                if (subItem.id == itemId) {
                    throw new IllegalArgumentException("Menu item with id " + itemId + " was already added");
                }
            }

            nestedMenuItems.add(
                    new ToolbarMenuSubItem(
                            itemId,
                            text,
                            enabled,
                            isCurrentlySelected,
                            value,
                            clickCallback
                    )
            );

            return this;
        }

        public MenuOverflowBuilder build() {
            return menuOverflowBuilder
                    .withSubItem(menuSubItem.id, menuSubItem.text, menuSubItem.enabled, null)
                    .addNestedItemsTo(menuSubItem.id, nestedMenuItems);
        }
    }
}
