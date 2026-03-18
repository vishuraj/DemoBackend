package com.demo.backend.service;

import com.demo.backend.model.Item;
import com.demo.backend.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    public Optional<Item> getItemById(Long id) {
        return itemRepository.findById(id);
    }

    public Item createItem(Item item) {
        return itemRepository.save(item);
    }

    public void deleteItem(Long id) {
        itemRepository.deleteById(id);
    }
}
