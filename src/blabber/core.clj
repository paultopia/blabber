(ns blabber.core
  (:require
    [clojure.string :refer [lower-case split]]
    [clojure.core.matrix.dataset :refer [dataset merge-datasets]]))

(def tolower lower-case)
; why?  because R and C use that and I don't want to memorize an extra name

(defn depunctuate
  "strip punctuation from string"
  [string]
  (apply str (filter #(or (Character/isLetter %) (Character/isDigit %) (Character/isSpace %)) string)))

(defn denumber
  "strip numbers from string"
  [string]
  (apply str (remove #(Character/isDigit %) string)))

(defn whitespace-split
  "split a vector of preprocessed strings into vector of vectors of strings on whitespace"
  [preprocessed-docs]
  (pmap #(split % #"\s") preprocessed-docs))
; needs to be able to accommodate a tokenizer function allowing ngrams etc.

(defn count-strings
  "count frequencies of strings in vector of vectors of strings"
  [stringvecs]
  (pmap frequencies stringvecs))

(defn list-strings
  "list all strings in doc set"
  [stringvecs]
  (distinct
    (apply concat stringvecs)))

(defn cartesian-map
  [stringlist]
  (zipmap stringlist (repeat 0)))

(defn sparsify-counts
  "based on strings in all preprocesed docs, fill counts with 0 for unused strings in each single preprocessed doc"
  [zeroes counts]
  (map #(merge-with + % zeroes) counts))

(defn unsorted-TD-map
  "split vector of preprocessed docs by spaces then make zero-filled map of counts"
  [preprocessed-docs]
  (let [stringvecs (whitespace-split preprocessed-docs)]
    (sparsify-counts
      (-> stringvecs list-strings cartesian-map)
      (-> stringvecs count-strings))))

(defn preprocessed-TD-matrix
  "make a core.matrix dataset from vector of preprocessed docs"
  [preprocessed-docs]
  (dataset (unsorted-TD-map preprocessed-docs)))

(defn make-TD-matrix
  "preprocess docs then make term document matrix out of them"
  ([docs]
   (preprocessed-TD-matrix docs))
  ([docs & funcs]
   (let [preproc (apply comp funcs)]
     (preprocessed-TD-matrix (pmap preproc docs)))))

(defn extract-texts
  "map of docs and labels, like from json --> extract the docs. all labels assumed strings"
  [docmap text-label]
  {:texts (map #(get % text-label) docmap) :features (map #(dissoc % text-label) docmap)})

(defn doc-feature-matrix
  "make combined matrix out of labelled data"
  ([docmap text-label]
   (merge-datasets (make-TD-matrix (:texts (extract-texts docmap text-label)))
                  (dataset (:features (extract-texts docmap text-label)))))
  ([docmap text-label & funcs]
   (merge-datasets (apply make-TD-matrix (:texts (extract-texts docmap text-label)) funcs)
                  (dataset (:features (extract-texts docmap text-label))))))



; this is just some test code for json functionality. will go away soon.
; (require 'clojure.data.json)
; (def datarecs (clojure.data.json/read-str (slurp "test.json")))
; (doc-feature-matrix datarecs "text")
; (doc-feature-matrix datarecs "text" denumber depunctuate tolower)

; ok, this needs some major cleanup but now I have the capacity to read a labelled json with text
; and get a labelled tdm out of it.

; probably the next step is to actually setup for tests.  That or do renaming and cleanup



; starting some ngram functionality
; assumption: I have a vector of tokens in original order and I want to extract ngrams of arbitrary size from it.


(defn ngram
  "re-tokenize word-tokenized string or char strvec into word-level ngrams of size n (no spaces)"
  [strvec n]
  (mapv #(apply str %) (partition n 1 strvec)))

(defn char-ngram
  "str --> character level ngrams of size n, including spaces (why? see http://www.aclweb.org/anthology/N15-1010 )"
  [strng n]
  (ngram (vec strng) n))

