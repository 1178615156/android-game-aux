# coding=utf-8
import cv2
import numpy as np


def jvm_find_pic(original, goal, patten):

    if (type(goal) is str):
        goal = cv2.imread(goal)
    if (type(original) is str):
        original = cv2.imread(original)
    if (patten == "edge"):
        goal = cv2.Canny(goal, 10, 10)
        original = cv2.Canny(original, 10, 10)

    result = cv2.matchTemplate(goal, original, cv2.TM_CCOEFF_NORMED)
    point = np.unravel_index(result.argmax(), result.shape)
    return str((np.max(result), point[1], point[0]))


# jvm_find_pic("./screen.png",
#              "./images/wdj.png",
#              "default")
