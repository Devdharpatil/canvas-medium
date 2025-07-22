from typing import List

def get_feedback(secret: str, guess: str) -> str:
    feedback = ['B'] * 5
    used_secret = [False] * 5
    used_guess = [False] * 5
    # First pass: Green
    for i in range(5):
        if secret[i] == guess[i]:
            feedback[i] = 'G'
            used_secret[i] = True
            used_guess[i] = True
    # Second pass: Yellow
    for i in range(5):
        if feedback[i] == 'G':
            continue
        for j in range(5):
            if not used_secret[j] and not used_guess[i] and guess[i] == secret[j] and i != j:
                feedback[i] = 'Y'
                used_secret[j] = True
                used_guess[i] = True
                break
    return ''.join(feedback)

def printPossibleResults(guess: int, response: str) -> List[int]:
    results = []
    guess_str = str(guess)
    for n in range(10000, 100000):
        s = str(n)
        if len(set(s)) != 5 or s[0] == '0':
            continue
        if get_feedback(s, guess_str) == response:
            results.append(n)
    return results

def run_tests():
    # Test 1: All green (only the guess itself is valid)
    guess = 12345
    resp = "GGGGG"
    res = printPossibleResults(guess, resp)
    assert res == [12345]
    # Test 2: All black (no digit in guess is in secret)
    guess = 12345
    resp = "BBBBB"
    res = printPossibleResults(guess, resp)
    for n in res:
        s = str(n)
        for c in str(guess):
            assert c not in s
    # Test 3: All yellow (all digits present but in wrong places)
    guess = 12345
    resp = "YYYYY"
    res = printPossibleResults(guess, resp)
    for n in res:
        s = str(n)
        for i in range(5):
            assert str(guess)[i] in s and s[i] != str(guess)[i]
    # Test 4: Mixed feedback
    guess = 12345
    resp = "GBYBB"
    res = printPossibleResults(guess, resp)
    for n in res:
        assert get_feedback(str(n), str(guess)) == resp
    # Test 5: Edge case, guess with 0
    guess = 10234
    resp = "GBBBB"
    res = printPossibleResults(guess, resp)
    for n in res:
        assert get_feedback(str(n), str(guess)) == resp
    # Test 6: No possible answer
    guess = 12345
    resp = "GGGGY"
    res = printPossibleResults(guess, resp)
    assert res == []
    # Test 7: Large output, but all valid
    guess = 98765
    resp = "BBBBB"
    res = printPossibleResults(guess, resp)
    for n in res:
        s = str(n)
        for c in str(guess):
            assert c not in s
    print("All tests passed!")

if __name__ == "__main__":
    run_tests()
    # Interactive
    guess = int(input("Enter guess: "))
    response = input("Enter response: ")
    res = printPossibleResults(guess, response)
    print(' '.join(map(str, res)))
