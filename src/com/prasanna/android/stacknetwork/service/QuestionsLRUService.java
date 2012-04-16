package com.prasanna.android.stacknetwork.service;

import com.prasanna.android.cache.LRU;
import com.prasanna.android.stacknetwork.model.Question;

public class QuestionsLRUService
{
    private QuestionsLRUService()
    {
    }

    private static int CACHE_SIZE = 15;
    private static final LRU<Long, Question> lru = new LRU<Long, Question>(CACHE_SIZE);

    public static void add(Question question)
    {
        if (question != null && question.id > 0)
        {
            lru.put(question.id, question);
        }
    }

    public static Question get(Long id)
    {
        Question question = null;

        if (id != null && id > 0)
        {
            question = lru.get(id);
        }
        
        return question;
    }
}
