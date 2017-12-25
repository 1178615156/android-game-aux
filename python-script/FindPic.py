import cv2
import numpy as np


def to_arr(image):
    return np.array(image)[:, :, 0:3]


class CvMatchTemple:
    def __init__(self, sim=0.95, original=None, goal=None):
        self.sim = sim
        self.__original = original
        self.__goal = goal
        self.__result = None

    def similarity(self, sim):
        return CvMatchTemple(
            sim=sim,
            original=self.__original,
            goal=self.__goal)

    def original(self, original):
        return CvMatchTemple(
            sim=self.sim,
            original=original,
            goal=self.__goal)

    def goal(self, goal):
        return CvMatchTemple(
            sim=self.sim,
            original=self.__original,
            goal=goal)

    def run(self):
        assert self.__goal is not None
        assert self.__original is not None
        goal = self.__goal
        original = self.__original
        if type(goal) is np.ndarray:
            pass
        elif type(goal) is str:
            goal = cv2.imread(goal)
        else:
            goal = to_arr(goal)

        if type(original) is np.ndarray:
            pass
        elif type(original) is str:
            original = cv2.imread(original)
        else:
            original = to_arr(original)

        result = cv2.matchTemplate(goal, original, cv2.TM_CCOEFF_NORMED)
        self.__result = result
        return self

    def result(self):
        if self.__result is None:
            self.run()
        return self.__result

    def startPoint(self):
        if self.maxValue() > self.sim:
            return self.maxPoint()
        else:
            return None

    def isFind(self):
        return self.startPoint() is not None

    def noFind(self):
        return not self.isFind()

    def maxValue(self):
        result = self.result()
        return np.max(self.result())

    def maxPoint(self):
        result = self.result()
        point = np.unravel_index(result.argmax(), result.shape)
        return (int(point[1]), int(point[0]))
