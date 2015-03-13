/*
 * <summary></summary>
 * <author>He Han</author>
 * <email>hankcs.cn@gmail.com</email>
 * <create-date>2014/10/8 14:07</create-date>
 *
 * <copyright file="TFDictionary.java" company="上海林原信息科技有限公司">
 * Copyright (c) 2003-2014, 上海林原信息科技有限公司. All Right Reserved, http://www.linrunsoft.com/
 * This source is subject to the LinrunSpace License. Please contact 上海林原信息科技有限公司 to get more information.
 * </copyright>
 */
package com.hankcs.hanlp.corpus.dictionary;

import com.hankcs.hanlp.corpus.io.IOUtil;
import com.hankcs.hanlp.corpus.occurrence.TermFrequency;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

/**
 * 词频词典
 * @author hankcs
 */
public class TFDictionary extends SimpleDictionary<TermFrequency> implements ISaveAble
{
    @Override
    protected Map.Entry<String, TermFrequency> onGenerateEntry(String line)
    {
        String[] param = line.split("=");
        return new AbstractMap.SimpleEntry<String, TermFrequency>(param[0], new TermFrequency(param[0], Integer.valueOf(param[1])));
    }

    public int combine(TFDictionary dictionary, int limit, boolean add)
    {
        int preSize = trie.size();
        for (Map.Entry<String, TermFrequency> entry : dictionary.trie.entrySet())
        {
            TermFrequency termFrequency = trie.get(entry.getKey());
            if (termFrequency == null)
            {
                trie.put(entry.getKey(), new TermFrequency(entry.getKey(), Math.min(limit, entry.getValue().getValue())));
            }
            else
            {
                if (add)
                {
                    termFrequency.setValue(termFrequency.getValue() + Math.min(limit, entry.getValue().getValue()));
                }
            }
        }
        return trie.size() - preSize;
    }

    public static int combine(String[] path)
    {
        TFDictionary dictionaryMain = new TFDictionary();
        dictionaryMain.load(path[0]);
        int preSize = dictionaryMain.trie.size();
        for (int i = 1; i < path.length; ++i)
        {
            TFDictionary dictionary = new TFDictionary();
            dictionary.load(path[i]);
            dictionaryMain.combine(dictionary, 1, true);
        }
        try
        {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path[0])));
            for (Map.Entry<String, TermFrequency> entry : dictionaryMain.trie.entrySet())
            {
                bw.write(entry.getKey());
                bw.write(' ');
                bw.write(String.valueOf(entry.getValue().getValue()));
                bw.newLine();
            }
            bw.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }

        return dictionaryMain.trie.size() - preSize;
    }

    /**
     * 获取频次
     * @param key
     * @return
     */
    public int getFrequency(String key)
    {
        TermFrequency termFrequency = get(key);
        if (termFrequency == null) return 0;
        return termFrequency.getFrequency();
    }

    public void add(String key)
    {
        TermFrequency termFrequency = trie.get(key);
        if (termFrequency == null)
        {
            termFrequency = new TermFrequency(key);
            trie.put(key, termFrequency);
        }
        else
        {
            termFrequency.increase();
        }
    }

    @Override
    public boolean saveTxtTo(String path)
    {
        LinkedList<TermFrequency> termFrequencyLinkedList = new LinkedList<TermFrequency>();
        for (Map.Entry<String, TermFrequency> entry : trie.entrySet())
        {
            termFrequencyLinkedList.add(entry.getValue());
        }
        return IOUtil.saveCollectionToTxt(termFrequencyLinkedList, path);
    }

    /**
     * 将值保存到文件
     * @param path
     * @return
     */
    public boolean saveKeyTo(String path)
    {
        LinkedList<String> keyList = new LinkedList<String>();
        for (Map.Entry<String, TermFrequency> entry : trie.entrySet())
        {
            keyList.add(entry.getKey());
        }
        return IOUtil.saveCollectionToTxt(keyList, path);
    }

    /**
     * 按照频率从高到低排序的条目
     * @return
     */
    public TreeSet<TermFrequency> values()
    {
        TreeSet<TermFrequency> set = new TreeSet<TermFrequency>(Collections.reverseOrder());

        for (Map.Entry<String, TermFrequency> entry : entrySet())
        {
            set.add(entry.getValue());
        }

        return set;
    }
}
