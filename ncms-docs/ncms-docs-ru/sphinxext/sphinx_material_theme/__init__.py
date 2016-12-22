import os

""" Sphinx material theme. Based on http://materializecss.com framework """

__author__ = "Adamansky Anton <adamansky@softmotions.com>"
__copyright__ = "Copyright 2015, Softmotions Ltd. <info@softmotions.com>"

VERSION = (1, 0, 0)

__version__ = ".".join(str(v) for v in VERSION)
__version_full__ = __version__


def get_html_theme_path():
    """Return list of HTML theme paths."""
    cur_dir = os.path.abspath(os.path.dirname(os.path.dirname(__file__)))
    return cur_dir
